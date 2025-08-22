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

    protected RoomJpaEntity() {}

    public RoomJpaEntity(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Room toRoom() {
        return new Room(this.id, this.name);
    }
}