package uk.co.kennah.chatter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class RoomController {

    @GetMapping("/api/rooms")
    public List<Room> getRooms() {
        // In a real application, this would come from a database or service
        return Arrays.asList(
            new Room("general", "General Chat"),
            new Room("tech-talk", "Tech Talk"),
            new Room("random", "Random Banter")
        );
    }
}

