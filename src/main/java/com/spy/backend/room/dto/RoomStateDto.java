package com.spy.backend.room.dto;

import java.util.List;

import com.spy.backend.room.Round;
import com.spy.backend.room.Room;
import com.spy.backend.room.RoomStatus;

/** Public room state. Never contains the phrase, category or spies. */
public record RoomStateDto(
		String code,
		RoomStatus status,
		List<PlayerDto> players,
		int roundNumber,
		String startingPlayerId) {

	public static RoomStateDto of(Room room) {
		synchronized (room) {
			RoomStatus status = room.status();
			boolean roundVisible = status == RoomStatus.IN_ROUND || status == RoomStatus.REVEALED;
			String startingPlayerId = roundVisible
					? room.currentRound().map(Round::startingPlayerId).orElse(null)
					: null;
			List<PlayerDto> players = room.players().stream()
					.map(p -> new PlayerDto(p.id(), p.nick(), room.isHost(p.id())))
					.toList();
			return new RoomStateDto(room.code(), status, players, room.roundNumber(), startingPlayerId);
		}
	}
}
