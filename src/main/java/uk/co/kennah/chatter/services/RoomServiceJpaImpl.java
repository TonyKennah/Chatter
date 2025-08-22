package uk.co.kennah.chatter.services;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import uk.co.kennah.chatter.model.Room;
import uk.co.kennah.chatter.model.RoomJpaEntity;
import uk.co.kennah.chatter.repo.RoomJpaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Profile("local")
public class RoomServiceJpaImpl implements RoomService {

    private final RoomJpaRepository repository;

    public RoomServiceJpaImpl(RoomJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Room> getAllRooms() {
        return repository.findAll().stream().map(RoomJpaEntity::toRoom).collect(Collectors.toList());
    }

    @Override
    public void createRoom(String id, String name) {
        repository.save(new RoomJpaEntity(id, name));
    }
}