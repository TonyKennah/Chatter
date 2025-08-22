package uk.co.kennah.chatter.model;

public class Room {
    private String id;
    private String name;
    private final String address;
    private final String audience;

    public Room(String id, String name, String address, String audience) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.audience = audience;
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