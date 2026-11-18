package com.spy.backend.room;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.spy.backend.auth.AuthenticatedPlayer;
import com.spy.backend.error.InvalidStateException;
import com.spy.backend.error.NickTakenException;
import com.spy.backend.error.NotEnoughPlayersException;
import com.spy.backend.error.RoomClosedException;
import com.spy.backend.error.RoomNotFoundException;
import com.spy.backend.error.ValidationException;
import com.spy.backend.room.dto.RevealDto;
import com.spy.backend.room.dto.RoomListDto;
import com.spy.backend.room.dto.RoomStateDto;
import com.spy.backend.room.dto.RoomSummaryDto;
import com.spy.backend.room.dto.StartRoundRequest;

@Service
public class RoomService {

	static final int MAX_NICK_LENGTH = 20;
	static final int MAX_PHRASE_LENGTH = 100;
	static final int MAX_CATEGORY_LENGTH = 50;
	static final int MIN_PLAYERS = 2;
	static final int MIN_LIST_LIMIT = 1;
	static final int MAX_LIST_LIMIT = 20;
	public static final int DEFAULT_LIST_LIMIT = 3;

	private final RoomRepository repository;
	private final TokenGenerator tokenGenerator;
	private final ApplicationEventPublisher events;
	private final Clock clock;

	public RoomService(RoomRepository repository, TokenGenerator tokenGenerator,
			ApplicationEventPublisher events, Clock clock) {
		this.repository = repository;
		this.tokenGenerator = tokenGenerator;
		this.events = events;
		this.clock = clock;
	}

	public Room createRoom(String rawNick) {
		Player host = newPlayer(validateNick(rawNick));
		return repository.create(code -> new Room(code, host));
	}

	public Player joinRoom(String code, String rawNick) {
		String nick = validateNick(rawNick);
		Room room = repository.find(code).orElseThrow(RoomNotFoundException::new);
		Player player;
		synchronized (room) {
			if (room.status() == RoomStatus.CLOSED) {
				throw new RoomClosedException();
			}
			if (room.isNickTaken(nick)) {
				throw new NickTakenException();
			}
			player = newPlayer(nick);
			room.addPlayer(player);
			room.touch(player.joinedAt());
		}
		events.publishEvent(new RoomEvent.PlayerJoined(room));
		return player;
	}

	public RoomStateDto startRound(AuthenticatedPlayer me, StartRoundRequest request) {
		me.requireHost();
		if (request == null) {
			throw new ValidationException("Missing request body");
		}
		String phrase = requireText("phrase", request.phrase(), MAX_PHRASE_LENGTH);
		String category = requireText("category", request.category(), MAX_CATEGORY_LENGTH);
		if (request.startingPlayerId() == null) {
			throw new ValidationException("startingPlayerId: must not be null");
		}
		if (request.spyPlayerIds() == null) {
			throw new ValidationException("spyPlayerIds: must not be null");
		}
		Set<String> spies = new LinkedHashSet<>(request.spyPlayerIds());

		Room room = me.room();
		RoomStateDto state;
		synchronized (room) {
			RoomStatus status = room.status();
			if (status != RoomStatus.LOBBY && status != RoomStatus.REVEALED) {
				throw new InvalidStateException("Round can be started only in LOBBY or REVEALED, room is " + status);
			}
			List<Player> players = room.players();
			if (players.size() < MIN_PLAYERS) {
				throw new NotEnoughPlayersException();
			}
			Set<String> playerIds = players.stream().map(Player::id).collect(Collectors.toSet());
			if (!playerIds.contains(request.startingPlayerId())) {
				throw new ValidationException("startingPlayerId: not a player of this room");
			}
			if (!playerIds.containsAll(spies)) {
				throw new ValidationException("spyPlayerIds: contains ids that are not players of this room");
			}
			Round round = new Round(room.roundNumber() + 1, phrase, category, request.showCategory(),
					new ArrayList<>(spies), request.startingPlayerId(), playerIds);
			room.startRound(round);
			room.touch(clock.instant());
			state = RoomStateDto.of(room);
		}
		events.publishEvent(new RoomEvent.RoundStarted(room));
		return state;
	}

	public RevealDto reveal(AuthenticatedPlayer me) {
		me.requireHost();
		Room room = me.room();
		RevealDto reveal;
		synchronized (room) {
			if (room.status() != RoomStatus.IN_ROUND) {
				throw new InvalidStateException("Reveal is possible only IN_ROUND, room is " + room.status());
			}
			room.reveal();
			room.touch(clock.instant());
			reveal = RevealDto.of(room.currentRound().orElseThrow());
		}
		events.publishEvent(new RoomEvent.RoundRevealed(room.code(), reveal));
		return reveal;
	}

	public RevealDto getReveal(AuthenticatedPlayer me) {
		Room room = me.room();
		synchronized (room) {
			if (room.status() != RoomStatus.REVEALED) {
				throw new InvalidStateException("Reveal is available only in REVEALED, room is " + room.status());
			}
			return RevealDto.of(room.currentRound().orElseThrow());
		}
	}

	public void closeRoom(AuthenticatedPlayer me) {
		me.requireHost();
		closeRoom(me.room());
	}

	/** Marks the room CLOSED, removes it and publishes {@link RoomEvent.RoomClosed}. */
	public void closeRoom(Room room) {
		closeRoomIf(room, null);
	}

	/**
	 * Closes the room only if its last activity is before {@code cutoff}. The check and the close happen
	 * under the room lock, so a request that touches the room concurrently keeps it alive.
	 *
	 * @return whether the room was closed
	 */
	public boolean closeIfInactiveSince(Room room, Instant cutoff) {
		return closeRoomIf(room, cutoff);
	}

	private boolean closeRoomIf(Room room, Instant inactiveBefore) {
		synchronized (room) {
			if (room.status() == RoomStatus.CLOSED) {
				return false;
			}
			if (inactiveBefore != null && !room.lastActivityAt().isBefore(inactiveBefore)) {
				return false;
			}
			room.close();
			repository.remove(room.code());
		}
		events.publishEvent(new RoomEvent.RoomClosed(room.code()));
		return true;
	}

	/**
	 * Lists open rooms, most recently active first, truncated to {@code limit}. Read-only: does not touch
	 * {@code lastActivityAt}, so listing never extends a room's life.
	 */
	public RoomListDto listRooms(int limit) {
		if (limit < MIN_LIST_LIMIT || limit > MAX_LIST_LIMIT) {
			throw new ValidationException("limit: must be " + MIN_LIST_LIMIT + "-" + MAX_LIST_LIMIT);
		}
		List<ListedRoom> open = new ArrayList<>();
		for (Room room : repository.all()) {
			synchronized (room) {
				RoomStatus status = room.status();
				if (status == RoomStatus.CLOSED) {
					continue;
				}
				List<Player> players = room.players();
				String hostNick = room.findPlayer(room.hostPlayerId()).map(Player::nick).orElse("");
				open.add(new ListedRoom(room.lastActivityAt(),
						new RoomSummaryDto(room.code(), hostNick, players.size(), status)));
			}
		}
		List<RoomSummaryDto> rooms = open.stream()
				.sorted(Comparator.comparing(ListedRoom::lastActivityAt).reversed())
				.limit(limit)
				.map(ListedRoom::summary)
				.toList();
		return new RoomListDto(rooms, open.size());
	}

	private record ListedRoom(Instant lastActivityAt, RoomSummaryDto summary) {
	}

	private Player newPlayer(String nick) {
		Instant now = clock.instant();
		return new Player(tokenGenerator.newPlayerId(), nick, tokenGenerator.newToken(), now);
	}

	private static String validateNick(String rawNick) {
		String nick = rawNick == null ? "" : rawNick.strip();
		if (nick.isEmpty() || nick.length() > MAX_NICK_LENGTH) {
			throw new ValidationException("nick: must be 1-" + MAX_NICK_LENGTH + " characters after trimming");
		}
		return nick;
	}

	private static String requireText(String field, String raw, int maxLength) {
		String value = raw == null ? "" : raw.strip();
		if (value.isEmpty() || value.length() > maxLength) {
			throw new ValidationException(field + ": must be 1-" + maxLength + " characters");
		}
		return value;
	}
}
