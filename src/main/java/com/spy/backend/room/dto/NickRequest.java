package com.spy.backend.room.dto;

/** Nick is trimmed and validated (1-20 chars) in {@code RoomService}. */
public record NickRequest(String nick) {
}
