package com.spy.backend.room.dto;

import java.util.List;

import com.spy.backend.room.Round;

public record RevealDto(int roundNumber, String phrase, String category, List<String> spyPlayerIds) {

	public static RevealDto of(Round round) {
		return new RevealDto(round.number(), round.phrase(), round.category(), round.spyPlayerIds());
	}
}
