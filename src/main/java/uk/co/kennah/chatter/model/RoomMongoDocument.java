package uk.co.kennah.chatter.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "rooms")
public class RoomMongoDocument {
    @Id
    private String id;
    private String name;

    // A no-arg constructor is needed for the MongoDB driver
    protected RoomMongoDocument() {}

    public RoomMongoDocument(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Room toRoom() {
        return new Room(this.id, this.name);
    }
}