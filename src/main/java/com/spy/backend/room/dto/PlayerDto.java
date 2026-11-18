package com.spy.backend.room.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PlayerDto(String id, String nick, @JsonProperty("isHost") boolean isHost) {
}
