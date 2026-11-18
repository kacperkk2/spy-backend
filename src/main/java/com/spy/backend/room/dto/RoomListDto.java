package com.spy.backend.room.dto;

import java.util.List;

/**
 * @param rooms most recently active open rooms first, at most {@code limit}
 * @param total number of all open rooms (may exceed {@code rooms.size()})
 */
public record RoomListDto(List<RoomSummaryDto> rooms, int total) {
}
