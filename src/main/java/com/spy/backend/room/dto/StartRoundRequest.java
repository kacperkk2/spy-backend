package com.spy.backend.room.dto;

import java.util.List;

/** Validated in {@code RoomService#startRound}. */
public record StartRoundRequest(
		String phrase,
		String category,
		boolean showCategory,
		List<String> spyPlayerIds,
		String startingPlayerId) {
}
