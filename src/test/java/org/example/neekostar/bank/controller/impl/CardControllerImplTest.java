package org.example.neekostar.bank.controller.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.neekostar.bank.dto.CardRequestDto;
import org.example.neekostar.bank.dto.CardResponseDto;
import org.example.neekostar.bank.service.CardService;
import org.example.neekostar.bank.util.AuthUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class CardControllerImplTest {

    private MockMvc mockMvc;

    @Mock
    private CardService cardService;

    private CardControllerImpl controller;

    private ObjectMapper objectMapper;

    private MockedStatic<AuthUtils> authUtilsMock;

    private UUID testUserId;
    private UUID testCardId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new CardControllerImpl(cardService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        authUtilsMock = org.mockito.Mockito.mockStatic(AuthUtils.class);
        testUserId = UUID.randomUUID();
        testCardId = UUID.randomUUID();

        authUtilsMock.when(AuthUtils::getCurrentUserId).thenReturn(testUserId);
    }

    @AfterEach
    void tearDown() {
        authUtilsMock.close();
    }

    @Test
    void createCard_ShouldReturnCreatedCard() throws Exception {
        CardRequestDto requestDto = new CardRequestDto();
        requestDto.setBalance(BigDecimal.valueOf(1000));
        requestDto.setExpiryDate(java.time.LocalDate.now().plusYears(3));

        CardResponseDto responseDto = new CardResponseDto();
        responseDto.setId(testCardId);
        responseDto.setBalance(requestDto.getBalance());
        responseDto.setExpiryDate(requestDto.getExpiryDate());
        responseDto.setMaskedNumber("**** **** **** 1234");

        when(cardService.createCard(eq(testUserId), any(CardRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/cards/" + testUserId + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testCardId.toString()))
                .andExpect(jsonPath("$.balance").value(1000))
                .andExpect(jsonPath("$.maskedNumber").value("**** **** **** 1234"));
    }

    @Test
    void getCard_ShouldReturnCard() throws Exception {
        CardResponseDto responseDto = new CardResponseDto();
        responseDto.setId(testCardId);
        responseDto.setBalance(BigDecimal.valueOf(1500));
        responseDto.setExpiryDate(java.time.LocalDate.now().plusYears(2));
        responseDto.setMaskedNumber("**** **** **** 5678");

        when(cardService.getCardById(testCardId)).thenReturn(responseDto);

        mockMvc.perform(get("/api/cards/" + testCardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCardId.toString()))
                .andExpect(jsonPath("$.balance").value(1500))
                .andExpect(jsonPath("$.maskedNumber").value("**** **** **** 5678"));
    }

    @Test
    void getCardBalance_ShouldReturnBalanceString() throws Exception {
        CardResponseDto responseDto = new CardResponseDto();
        responseDto.setBalance(BigDecimal.valueOf(2000));

        when(cardService.getCardById(testCardId)).thenReturn(responseDto);

        mockMvc.perform(get("/api/cards/" + testCardId + "/balance"))
                .andExpect(status().isOk())
                .andExpect(content().string("Balance: 2000$"));
    }

    @Test
    void getCardsForUser_ShouldReturnPagedCards() throws Exception {
        CardResponseDto card1 = new CardResponseDto();
        card1.setId(UUID.randomUUID());
        card1.setBalance(BigDecimal.valueOf(100));
        card1.setMaskedNumber("**** **** **** 1111");

        CardResponseDto card2 = new CardResponseDto();
        card2.setId(UUID.randomUUID());
        card2.setBalance(BigDecimal.valueOf(200));
        card2.setMaskedNumber("**** **** **** 2222");

        Page<CardResponseDto> page = new PageImpl<>(List.of(card1, card2), PageRequest.of(0, 10), 2);

        when(cardService.getCardsForCurrentUser(testUserId, 0, 10, PageRequest.of(0, 10))).thenReturn(page);

        mockMvc.perform(get("/api/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].maskedNumber").value("**** **** **** 1111"));
    }

    @Test
    void filterCards_ShouldReturnPagedFilteredCards() throws Exception {
        CardResponseDto card = new CardResponseDto();
        card.setId(UUID.randomUUID());
        card.setBalance(BigDecimal.valueOf(300));
        card.setMaskedNumber("**** **** **** 3333");

        Page<CardResponseDto> page = new PageImpl<>(List.of(card), PageRequest.of(0, 10), 1);

        when(cardService.filterCards(testUserId, 0, 10, "ACTIVE", PageRequest.of(0, 10))).thenReturn(page);

        mockMvc.perform(get("/api/cards/filter")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].maskedNumber").value("**** **** **** 3333"));
    }

    @Test
    void blockCard_ShouldReturnNoContent() throws Exception {
        doNothing().when(cardService).blockCard(testCardId);

        mockMvc.perform(patch("/api/cards/" + testCardId + "/block"))
                .andExpect(status().isNoContent());
    }

    @Test
    void activateCard_ShouldReturnNoContent() throws Exception {
        doNothing().when(cardService).activateCard(testCardId);

        mockMvc.perform(patch("/api/cards/" + testCardId + "/activate"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCard_ShouldReturnNoContent() throws Exception {
        doNothing().when(cardService).deleteCard(testCardId);

        mockMvc.perform(patch("/api/cards/" + testCardId + "/delete"))
                .andExpect(status().isNoContent());
    }
}
