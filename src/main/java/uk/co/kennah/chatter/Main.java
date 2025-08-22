package uk.co.kennah.chatter;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import uk.co.kennah.chatter.services.RoomService;

import java.net.InetAddress;
import java.util.Objects;
import java.util.stream.Stream;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public CommandLineRunner dataSeeder(RoomService roomService) {
        return args -> {
            // In an auto-scaling environment, get a unique ID.
            // Priority: System Property (-Dinstance.id=...) > Environment Variable (INSTANCE_ID) > Hostname Fallback
            String instanceId = Stream.of(System.getProperty("instance.id"), System.getenv("INSTANCE_ID"))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseGet(() -> {
                        try {
                            // Fallback to hostname, which is often unique in cloud/container environments.
                            return InetAddress.getLocalHost().getHostName();
                        } catch (Exception e) {
                            // A final fallback for local development if hostname fails.
                            return "local-dev-instance";
                        }
                    });

            String roomName = "Chat on " + instanceId;

            // In a real application, you might want to check if the room already exists
            // before creating it, to handle instance restarts gracefully.
            roomService.createRoom(instanceId, roomName, "dynamic-address-for-" + instanceId, "general-audience");
        };
    }
}
