package org.example.neekostar.bank.controller.impl;

import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.neekostar.bank.dto.CardBlockRequestDto;
import org.example.neekostar.bank.dto.CardBlockResponseDto;
import org.example.neekostar.bank.service.CardBlockRequestService;
import org.example.neekostar.bank.util.AuthUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class CardBlockRequestControllerImplTest {

    private MockMvc mockMvc;

    @Mock
    private CardBlockRequestService cardBlockRequestService;

    private CardBlockRequestControllerImpl controller;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockedStatic<AuthUtils> authUtilsMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new CardBlockRequestControllerImpl(cardBlockRequestService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        authUtilsMock = mockStatic(AuthUtils.class);
        authUtilsMock.when(AuthUtils::getCurrentUserId).thenReturn(UUID.randomUUID());
    }

    @AfterEach
    void tearDown() {
        authUtilsMock.close();
    }

    @Test
    @WithMockUser(roles = "USER")
    void submit_ShouldReturnOk() throws Exception {
        CardBlockRequestDto requestDto = new CardBlockRequestDto();
        requestDto.setCardId(UUID.randomUUID());
        requestDto.setReason("Test reason");

        doNothing().when(cardBlockRequestService).submitRequest(any(UUID.class), any(CardBlockRequestDto.class));

        mockMvc.perform(post("/api/card-block-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void pending_ShouldReturnList() throws Exception {
        CardBlockResponseDto responseDto = new CardBlockResponseDto();
        responseDto.setCardId(UUID.randomUUID());
        responseDto.setReason("Test reason");
        responseDto.setStatus("PENDING");

        when(cardBlockRequestService.getPendingRequests()).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/api/card-block-requests/pending")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cardId").value(responseDto.getCardId().toString()))
                .andExpect(jsonPath("$[0].reason").value("Test reason"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approve_ShouldReturnOk() throws Exception {
        UUID requestId = UUID.randomUUID();

        doNothing().when(cardBlockRequestService).approveRequest(requestId);

        mockMvc.perform(post("/api/card-block-requests/{requestId}/approve", requestId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reject_ShouldReturnOk() throws Exception {
        UUID requestId = UUID.randomUUID();

        doNothing().when(cardBlockRequestService).rejectRequest(requestId);

        mockMvc.perform(post("/api/card-block-requests/{requestId}/reject", requestId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
