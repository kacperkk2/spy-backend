package com.spy.backend.room;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Mutable room state. All access goes through the room's monitor: single methods are
 * {@code synchronized}, and multi-step operations (check-then-act) should wrap them in
 * {@code synchronized (room) { ... }}.
 */
public class Room {

	private final String code;
	private final String hostPlayerId;
	private final List<Player> players = new ArrayList<>();
	private RoomStatus status = RoomStatus.LOBBY;
	private Round currentRound;
	private Instant lastActivityAt;

	public Room(String code, Player host) {
		this.code = Objects.requireNonNull(code);
		this.hostPlayerId = host.id();
		this.players.add(host);
		this.lastActivityAt = host.joinedAt();
	}

	public String code() {
		return code;
	}

	public String hostPlayerId() {
		return hostPlayerId;
	}

	public synchronized RoomStatus status() {
		return status;
	}

	public synchronized List<Player> players() {
		return List.copyOf(players);
	}

	public synchronized int roundNumber() {
		return currentRound == null ? 0 : currentRound.number();
	}

	public synchronized Optional<Round> currentRound() {
		return Optional.ofNullable(currentRound);
	}

	public synchronized Instant lastActivityAt() {
		return lastActivityAt;
	}

	public boolean isHost(String playerId) {
		return hostPlayerId.equals(playerId);
	}

	public synchronized Optional<Player> findPlayer(String playerId) {
		return players.stream().filter(p -> p.id().equals(playerId)).findFirst();
	}

	public synchronized Optional<Player> findPlayerByToken(String token) {
		if (token == null || token.isEmpty()) {
			return Optional.empty();
		}
		byte[] given = token.getBytes(StandardCharsets.UTF_8);
		return players.stream()
				.filter(p -> MessageDigest.isEqual(p.token().getBytes(StandardCharsets.UTF_8), given))
				.findFirst();
	}

	public synchronized boolean isNickTaken(String nick) {
		String normalized = normalizeNick(nick);
		return players.stream().anyMatch(p -> normalizeNick(p.nick()).equals(normalized));
	}

	public synchronized void addPlayer(Player player) {
		players.add(player);
	}

	public synchronized void startRound(Round round) {
		this.currentRound = round;
		this.status = RoomStatus.IN_ROUND;
	}

	public synchronized void reveal() {
		this.status = RoomStatus.REVEALED;
	}

	public synchronized void close() {
		this.status = RoomStatus.CLOSED;
	}

	public synchronized void touch(Instant now) {
		this.lastActivityAt = now;
	}

	private static String normalizeNick(String nick) {
		return nick.trim().toLowerCase(Locale.ROOT);
	}
}
