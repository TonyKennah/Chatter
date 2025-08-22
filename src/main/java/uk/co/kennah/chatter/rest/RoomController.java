package uk.co.kennah.chatter.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.co.kennah.chatter.model.Room;
import uk.co.kennah.chatter.services.RoomService;

import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@RestController
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/api/rooms")
    public List<Room> getRooms() {
        // Rooms are now loaded from the application configuration
        return roomService.getAllRooms();
    }
}