package com.spy.backend.room;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;

import org.springframework.stereotype.Component;

@Component
public class RoomCodeGenerator {

	public static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
	public static final int LENGTH = 5;

	private final RandomGenerator random;

	public RoomCodeGenerator() {
		this(new SecureRandom());
	}

	RoomCodeGenerator(RandomGenerator random) {
		this.random = random;
	}

	public String next() {
		char[] code = new char[LENGTH];
		for (int i = 0; i < LENGTH; i++) {
			code[i] = ALPHABET.charAt(random.nextInt(ALPHABET.length()));
		}
		return new String(code);
	}
}
