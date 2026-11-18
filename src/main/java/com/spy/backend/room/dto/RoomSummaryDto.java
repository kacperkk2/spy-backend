package com.spy.backend.room.dto;

import com.spy.backend.room.RoomStatus;

/** Public summary of an open room on the room list. No tokens, player ids or round data. */
public record RoomSummaryDto(String code, String hostNick, int playersCount, RoomStatus status) {
}
