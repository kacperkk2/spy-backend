package com.spy.backend.auth;

import com.spy.backend.error.NotHostException;
import com.spy.backend.room.Player;
import com.spy.backend.room.Room;

public record AuthenticatedPlayer(Room room, Player player) {

	public boolean isHost() {
		return room.isHost(player.id());
	}

	/** @throws NotHostException (403) when the player is not the room's host */
	public AuthenticatedPlayer requireHost() {
		if (!isHost()) {
			throw new NotHostException();
		}
		return this;
	}
}
