package com.spy.backend.room;

import com.spy.backend.room.dto.RevealDto;

/**
 * Application events published after a room changes (outside the room lock).
 * Task 08 maps them to SSE: room-state, round-started, round-revealed, room-closed.
 */
public sealed interface RoomEvent {

	String code();

	record PlayerJoined(Room room) implements RoomEvent {
		public String code() {
			return room.code();
		}
	}

	record RoundStarted(Room room) implements RoomEvent {
		public String code() {
			return room.code();
		}
	}

	record RoundRevealed(String code, RevealDto reveal) implements RoomEvent {
	}

	record RoomClosed(String code) implements RoomEvent {
	}
}
