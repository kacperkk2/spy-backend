package com.spy.backend.room;

import java.time.Instant;
import java.util.Objects;

public record Player(String id, String nick, String token, Instant joinedAt) {

	public Player {
		Objects.requireNonNull(id);
		Objects.requireNonNull(nick);
		Objects.requireNonNull(token);
		Objects.requireNonNull(joinedAt);
	}

	@Override
	public String toString() {
		return "Player[id=" + id + ", nick=" + nick + ", joinedAt=" + joinedAt + "]";
	}
}
