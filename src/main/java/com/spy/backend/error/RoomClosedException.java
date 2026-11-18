package com.spy.backend.error;

public class RoomClosedException extends ApiException {

	public RoomClosedException() {
		this("Room is closed");
	}

	public RoomClosedException(String message) {
		super(ErrorCode.ROOM_CLOSED, message);
	}
}
