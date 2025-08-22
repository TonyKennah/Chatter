package uk.co.kennah.chatter.services;

import java.util.List;

import uk.co.kennah.chatter.model.Room;

public interface RoomService {

    List<Room> getAllRooms();

    void createRoom(String id, String name, String address, String audience);

}