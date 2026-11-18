package com.spy.backend.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
	ROOM_NOT_FOUND(HttpStatus.NOT_FOUND),
	ROOM_CLOSED(HttpStatus.GONE),
	NICK_TAKEN(HttpStatus.CONFLICT),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
	NOT_HOST(HttpStatus.FORBIDDEN),
	INVALID_STATE(HttpStatus.CONFLICT),
	VALIDATION(HttpStatus.BAD_REQUEST),
	NOT_ENOUGH_PLAYERS(HttpStatus.CONFLICT);

	private final HttpStatus status;

	ErrorCode(HttpStatus status) {
		this.status = status;
	}

	public HttpStatus status() {
		return status;
	}
}
