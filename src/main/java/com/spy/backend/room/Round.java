package com.spy.backend.room;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public record Round(
		int number,
		String phrase,
		String category,
		boolean showCategory,
		List<String> spyPlayerIds,
		String startingPlayerId,
		Set<String> participantIds) {

	public Round {
		Objects.requireNonNull(phrase);
		Objects.requireNonNull(category);
		Objects.requireNonNull(startingPlayerId);
		spyPlayerIds = List.copyOf(spyPlayerIds);
		participantIds = Set.copyOf(participantIds);
	}

	public boolean isParticipant(String playerId) {
		return participantIds.contains(playerId);
	}

	public boolean isSpy(String playerId) {
		return spyPlayerIds.contains(playerId);
	}
}
