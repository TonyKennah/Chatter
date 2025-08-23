package uk.co.kennah.chatter.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rooms")
public class RoomJpaEntity {
    @Id
    private String id;
    private String name;
    private String address;
    private String audience;

    protected RoomJpaEntity() {}

    public RoomJpaEntity(String id, String name, String address, String audience) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.audience = audience;
    }

    public Room toRoom() {
        return new Room(this.id, this.name, this.address, this.audience);
    }

    public static RoomJpaEntity fromRoom(Room room) {
        return new RoomJpaEntity(
                room.getId(),
                room.getName(),
                room.getAddress(),
                room.getAudience()
        );
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getAudience() {
        return audience;
    }
}