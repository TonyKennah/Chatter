package uk.co.kennah.chatter.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.kennah.chatter.model.Room;
import uk.co.kennah.chatter.model.RoomJpaEntity;
import uk.co.kennah.chatter.repo.RoomJpaRepository;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
public class RoomServiceJpaImplTest {

    @Mock
    private RoomJpaRepository repository;

    @InjectMocks
    private RoomServiceJpaImpl roomService;

    @Test
    public void getAllRooms_shouldReturnListOfRooms() {
        RoomJpaEntity entity1 = new RoomJpaEntity("1", "Room 1", "Address 1", "Audience 1");
        RoomJpaEntity entity2 = new RoomJpaEntity("2", "Room 2", "Address 2", "Audience 2");
        List<RoomJpaEntity> entities = Arrays.asList(entity1, entity2);

        when(repository.findAll()).thenReturn(entities);

        List<Room> rooms = roomService.getAllRooms();

        verify(repository).findAll();

        assertEquals(2, rooms.size());

        Room room1 = rooms.get(0);
        assertEquals("1", room1.getId());
        assertEquals("Room 1", room1.getName());
        assertEquals("Address 1", room1.getAddress());
        assertEquals("Audience 1", room1.getAudience());

        Room room2 = rooms.get(1);
        assertEquals("2", room2.getId());
        assertEquals("Room 2", room2.getName());
        assertEquals("Address 2", room2.getAddress());
        assertEquals("Audience 2", room2.getAudience());
    }

    @Test
    public void createRoom_shouldSaveRoom() {
        ArgumentCaptor<RoomJpaEntity> captor = ArgumentCaptor.forClass(RoomJpaEntity.class);

        roomService.createRoom("1", "Room 1", "Address 1", "Audience 1");

        verify(repository).save(captor.capture());

        RoomJpaEntity savedEntity = captor.getValue();
        assertEquals("1", savedEntity.getId());
        assertEquals("Room 1", savedEntity.getName());
        assertEquals("Address 1", savedEntity.getAddress());
        assertEquals("Audience 1", savedEntity.getAudience());
    }
}
