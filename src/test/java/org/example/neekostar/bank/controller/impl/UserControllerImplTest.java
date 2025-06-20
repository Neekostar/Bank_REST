package org.example.neekostar.bank.controller.impl;

import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.neekostar.bank.dto.UserResponseDto;
import org.example.neekostar.bank.dto.UserUpdateDto;
import org.example.neekostar.bank.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class UserControllerImplTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    private UserControllerImpl controller;

    private ObjectMapper objectMapper;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new UserControllerImpl(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        testUserId = UUID.randomUUID();
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {
        UserResponseDto userDto = new UserResponseDto();
        userDto.setId(testUserId);
        userDto.setFirstName("Ivan");
        userDto.setLastName("Ivanov");
        userDto.setEmail("ivan@example.com");

        when(userService.getById(testUserId)).thenReturn(userDto);

        mockMvc.perform(get("/api/users/" + testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.firstName").value("Ivan"))
                .andExpect(jsonPath("$.lastName").value("Ivanov"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setFirstName("Petr");
        updateDto.setLastName("Petrov");

        UserResponseDto updatedUser = new UserResponseDto();
        updatedUser.setId(testUserId);
        updatedUser.setFirstName("Petr");
        updatedUser.setLastName("Petrov");
        updatedUser.setEmail("ivan@example.com");

        when(userService.updateUser(eq(testUserId), any(UserUpdateDto.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/" + testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.firstName").value("Petr"))
                .andExpect(jsonPath("$.lastName").value("Petrov"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"));
    }

    @Test
    void getAllUsers_ShouldReturnUserList() throws Exception {
        UserResponseDto user1 = new UserResponseDto();
        user1.setId(UUID.randomUUID());
        user1.setFirstName("Ivan");
        user1.setLastName("Ivanov");
        user1.setEmail("ivan@example.com");

        UserResponseDto user2 = new UserResponseDto();
        user2.setId(UUID.randomUUID());
        user2.setFirstName("Petr");
        user2.setLastName("Petrov");
        user2.setEmail("petr@example.com");

        when(userService.getAllUsers()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("Ivan"))
                .andExpect(jsonPath("$[1].firstName").value("Petr"));
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUser(testUserId);

        mockMvc.perform(delete("/api/users/" + testUserId))
                .andExpect(status().isNoContent());
    }
}
