package com.spy.backend.room;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.spy.backend.auth.AuthenticatedPlayer;
import com.spy.backend.auth.CurrentPlayer;
import com.spy.backend.room.dto.CreateRoomResponse;
import com.spy.backend.room.dto.JoinRoomResponse;
import com.spy.backend.room.dto.MyRoleDto;
import com.spy.backend.room.dto.NickRequest;
import com.spy.backend.room.dto.RevealDto;
import com.spy.backend.room.dto.RoomListDto;
import com.spy.backend.room.dto.RoomStateDto;
import com.spy.backend.room.dto.StartRoundRequest;

@RestController
@RequestMapping("/rooms")
public class RoomController {

	private final RoomService roomService;

	public RoomController(RoomService roomService) {
		this.roomService = roomService;
	}

	@GetMapping
	public RoomListDto list(@RequestParam(required = false) Integer limit) {
		return roomService.listRooms(limit == null ? RoomService.DEFAULT_LIST_LIMIT : limit);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CreateRoomResponse create(@RequestBody NickRequest request) {
		Room room = roomService.createRoom(request.nick());
		Player host = room.players().getFirst();
		return new CreateRoomResponse(room.code(), host.id(), host.token());
	}

	@PostMapping("/{code}/players")
	@ResponseStatus(HttpStatus.CREATED)
	public JoinRoomResponse join(@PathVariable String code, @RequestBody NickRequest request) {
		Player player = roomService.joinRoom(code, request.nick());
		return new JoinRoomResponse(player.id(), player.token());
	}

	@GetMapping("/{code}")
	public RoomStateDto state(@CurrentPlayer AuthenticatedPlayer me) {
		return RoomStateDto.of(me.room());
	}

	@GetMapping("/{code}/me")
	public MyRoleDto myRole(@CurrentPlayer AuthenticatedPlayer me) {
		return MyRoleDto.of(me.room(), me.player());
	}

	@PostMapping("/{code}/rounds")
	public RoomStateDto startRound(@CurrentPlayer AuthenticatedPlayer me,
			@RequestBody(required = false) StartRoundRequest request) {
		return roomService.startRound(me, request);
	}

	@PostMapping("/{code}/rounds/current/reveal")
	public RevealDto reveal(@CurrentPlayer AuthenticatedPlayer me) {
		return roomService.reveal(me);
	}

	@GetMapping("/{code}/reveal")
	public RevealDto getReveal(@CurrentPlayer AuthenticatedPlayer me) {
		return roomService.getReveal(me);
	}

	@DeleteMapping("/{code}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void close(@CurrentPlayer AuthenticatedPlayer me) {
		roomService.closeRoom(me);
	}
}
