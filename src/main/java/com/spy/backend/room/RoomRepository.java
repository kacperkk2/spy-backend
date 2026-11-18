package com.spy.backend.room;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import org.springframework.stereotype.Repository;

@Repository
public class RoomRepository {

	static final int MAX_CODE_ATTEMPTS = 50;

	private final ConcurrentMap<String, Room> rooms = new ConcurrentHashMap<>();
	private final RoomCodeGenerator codeGenerator;

	public RoomRepository(RoomCodeGenerator codeGenerator) {
		this.codeGenerator = codeGenerator;
	}

	/**
	 * Creates a room under a fresh, unused code. {@code factory} receives the code and builds the room;
	 * on collision a new code is drawn.
	 */
	public Room create(Function<String, Room> factory) {
		for (int attempt = 0; attempt < MAX_CODE_ATTEMPTS; attempt++) {
			String code = codeGenerator.next();
			if (rooms.containsKey(code)) {
				continue;
			}
			Room room = factory.apply(code);
			if (rooms.putIfAbsent(code, room) == null) {
				return room;
			}
		}
		throw new IllegalStateException("Could not generate a unique room code");
	}

	public Optional<Room> find(String code) {
		return code == null ? Optional.empty() : Optional.ofNullable(rooms.get(normalizeCode(code)));
	}

	public Optional<Room> remove(String code) {
		return code == null ? Optional.empty() : Optional.ofNullable(rooms.remove(normalizeCode(code)));
	}

	public Collection<Room> all() {
		return List.copyOf(rooms.values());
	}

	public static String normalizeCode(String code) {
		return code.trim().toUpperCase(Locale.ROOT);
	}
}
