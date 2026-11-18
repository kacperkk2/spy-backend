package com.spy.backend.room.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spy.backend.room.Player;
import com.spy.backend.room.Room;
import com.spy.backend.room.Round;

/** Role of the token owner in the current (or, when REVEALED, the last) round. */
public record MyRoleDto(
		String playerId,
		boolean participating,
		@JsonProperty("isSpy") boolean isSpy,
		String phrase,
		String category,
		@JsonProperty("isStarting") boolean isStarting) {

	public static MyRoleDto of(Room room, Player player) {
		String id = player.id();
		Round round = room.currentRound().orElse(null);
		if (round == null || !round.isParticipant(id)) {
			return new MyRoleDto(id, false, false, null, null, false);
		}
		boolean spy = round.isSpy(id);
		return new MyRoleDto(
				id,
				true,
				spy,
				spy ? null : round.phrase(),
				round.showCategory() ? round.category() : null,
				round.startingPlayerId().equals(id));
	}
}
