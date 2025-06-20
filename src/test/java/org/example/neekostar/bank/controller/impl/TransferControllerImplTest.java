package org.example.neekostar.bank.controller.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.neekostar.bank.dto.TransferRequestDto;
import org.example.neekostar.bank.dto.TransferResponseDto;
import org.example.neekostar.bank.service.TransferService;
import org.example.neekostar.bank.util.AuthUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class TransferControllerImplTest {

    private MockMvc mockMvc;

    @Mock
    private TransferService transferService;

    private TransferControllerImpl controller;

    private ObjectMapper objectMapper;

    private MockedStatic<AuthUtils> authUtilsMock;

    private UUID testUserId;
    private UUID testCardIdFrom;
    private UUID testCardIdTo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new TransferControllerImpl(transferService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        objectMapper = new ObjectMapper();

        authUtilsMock = mockStatic(AuthUtils.class);
        testUserId = UUID.randomUUID();
        testCardIdFrom = UUID.randomUUID();
        testCardIdTo = UUID.randomUUID();

        authUtilsMock.when(AuthUtils::getCurrentUserId).thenReturn(testUserId);
    }

    @AfterEach
    void tearDown() {
        authUtilsMock.close();
    }

    @Test
    void transfer_ShouldReturnCreatedTransfer() throws Exception {
        TransferRequestDto requestDto = new TransferRequestDto();
        requestDto.setFromCardId(testCardIdFrom);
        requestDto.setToCardId(testCardIdTo);
        requestDto.setAmount(BigDecimal.valueOf(100));

        TransferResponseDto responseDto = new TransferResponseDto();
        responseDto.setId(UUID.randomUUID());
        responseDto.setFromCardNumber("**** **** **** 1111");
        responseDto.setToCardNumber("**** **** **** 2222");
        responseDto.setAmount(requestDto.getAmount());
        responseDto.setTimestamp(LocalDateTime.now());

        when(transferService.createTransfer(eq(testUserId), any(TransferRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(responseDto.getId().toString()))
                .andExpect(jsonPath("$.fromCardNumber").value("**** **** **** 1111"))
                .andExpect(jsonPath("$.toCardNumber").value("**** **** **** 2222"))
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getTransferHistory_ShouldReturnTransferList() throws Exception {
        TransferResponseDto transfer1 = new TransferResponseDto();
        transfer1.setId(UUID.randomUUID());
        transfer1.setFromCardNumber("**** **** **** 1111");
        transfer1.setToCardNumber("**** **** **** 2222");
        transfer1.setAmount(BigDecimal.valueOf(50));
        transfer1.setTimestamp(LocalDateTime.now().minusDays(1));

        TransferResponseDto transfer2 = new TransferResponseDto();
        transfer2.setId(UUID.randomUUID());
        transfer2.setFromCardNumber("**** **** **** 3333");
        transfer2.setToCardNumber("**** **** **** 4444");
        transfer2.setAmount(BigDecimal.valueOf(75));
        transfer2.setTimestamp(LocalDateTime.now());

        List<TransferResponseDto> transfers = List.of(transfer1, transfer2);

        when(transferService.getTransferHistory(testCardIdFrom)).thenReturn(transfers);

        mockMvc.perform(get("/api/transfers/history/" + testCardIdFrom))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(transfer1.getId().toString()))
                .andExpect(jsonPath("$[0].fromCardNumber").value("**** **** **** 1111"))
                .andExpect(jsonPath("$[0].toCardNumber").value("**** **** **** 2222"))
                .andExpect(jsonPath("$[0].amount").value(50))
                .andExpect(jsonPath("$[0].timestamp").exists())
                .andExpect(jsonPath("$[1].id").value(transfer2.getId().toString()))
                .andExpect(jsonPath("$[1].fromCardNumber").value("**** **** **** 3333"))
                .andExpect(jsonPath("$[1].toCardNumber").value("**** **** **** 4444"))
                .andExpect(jsonPath("$[1].amount").value(75))
                .andExpect(jsonPath("$[1].timestamp").exists());
    }
}
