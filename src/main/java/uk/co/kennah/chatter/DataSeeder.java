package uk.co.kennah.chatter;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import uk.co.kennah.chatter.services.RoomService;

@Component
public class DataSeeder implements CommandLineRunner {

    private final RoomService roomService;

    public DataSeeder(RoomService roomService) {
        this.roomService = roomService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed data only if no rooms exist
        if (roomService.getAllRooms().isEmpty()) {
            roomService.createRoom("general", "General Chat");
            roomService.createRoom("tech-talk", "Tech Talk");
            roomService.createRoom("random", "Random Banter");
        }
    }
}