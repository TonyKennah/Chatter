package uk.co.kennah.chatter.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.kennah.chatter.model.Room;
import uk.co.kennah.chatter.services.RoomService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(RoomController.class)
public class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomService roomService;

    @Test
    public void getRooms_shouldReturnListOfRooms() throws Exception {
        Room room1 = new Room("1", "Room 1", "Address 1", "Audience 1");
        Room room2 = new Room("2", "Room 2", "Address 2", "Audience 2");
        List<Room> rooms = Arrays.asList(room1, room2);

        when(roomService.getAllRooms()).thenReturn(rooms);

        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Room 1"))
                .andExpect(jsonPath("$[1].name").value("Room 2"));
    }
}
