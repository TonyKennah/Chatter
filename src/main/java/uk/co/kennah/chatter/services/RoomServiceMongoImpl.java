package uk.co.kennah.chatter.services;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import uk.co.kennah.chatter.model.Room;
import uk.co.kennah.chatter.model.RoomMongoDocument;
import uk.co.kennah.chatter.repo.RoomMongoRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Profile("prod")
public class RoomServiceMongoImpl implements RoomService {

    private final RoomMongoRepository repository;

    public RoomServiceMongoImpl(RoomMongoRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Room> getAllRooms() {
        return repository.findAll().stream().map(RoomMongoDocument::toRoom).collect(Collectors.toList());
    }

    @Override
    public void createRoom(String id, String name) {
        repository.save(new RoomMongoDocument(id, name));
    }
}