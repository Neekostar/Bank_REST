package org.example.neekostar.bank.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.example.neekostar.bank.dto.TransferRequestDto;
import org.example.neekostar.bank.dto.TransferResponseDto;
import org.example.neekostar.bank.entity.Card;
import org.example.neekostar.bank.entity.CardStatus;
import org.example.neekostar.bank.entity.Transfer;
import org.example.neekostar.bank.entity.User;
import org.example.neekostar.bank.exception.InvalidArgumentException;
import org.example.neekostar.bank.exception.NotFoundException;
import org.example.neekostar.bank.mapper.TransferMapper;
import org.example.neekostar.bank.repository.CardRepository;
import org.example.neekostar.bank.repository.TransferRepository;
import org.example.neekostar.bank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

public class TransferServiceImplTest {

    private TransferServiceImpl transferService;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransferMapper transferMapper;

    private UUID userId;
    private UUID fromCardId;
    private UUID toCardId;

    private Card fromCard;
    private Card toCard;

    private TransferRequestDto transferRequestDto;
    private Transfer transfer;
    private TransferResponseDto transferResponseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        transferService = new TransferServiceImpl(cardRepository, transferRepository, userRepository, transferMapper);

        userId = UUID.randomUUID();
        fromCardId = UUID.randomUUID();
        toCardId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        fromCard = new Card();
        fromCard.setId(fromCardId);
        fromCard.setOwner(user);
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setBalance(new BigDecimal("1000.00"));

        toCard = new Card();
        toCard.setId(toCardId);
        toCard.setOwner(user);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setBalance(new BigDecimal("500.00"));

        transferRequestDto = new TransferRequestDto();
        transferRequestDto.setFromCardId(fromCardId);
        transferRequestDto.setToCardId(toCardId);
        transferRequestDto.setAmount(new BigDecimal("200.00"));

        transfer = new Transfer();
        transfer.setFromCard(fromCard);
        transfer.setToCard(toCard);
        transfer.setAmount(new BigDecimal("200.00"));

        transferResponseDto = new TransferResponseDto();
    }

    @Test
    void createTransfer_ShouldReturnDto_WhenSuccessful() {
        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
        when(transferMapper.toDto(any(Transfer.class))).thenReturn(transferResponseDto);

        TransferResponseDto result = transferService.createTransfer(userId, transferRequestDto);

        assertThat(result).isEqualTo(transferResponseDto);

        assertThat(fromCard.getBalance()).isEqualByComparingTo("800.00");
        assertThat(toCard.getBalance()).isEqualByComparingTo("700.00");

        verify(cardRepository).save(fromCard);
        verify(cardRepository).save(toCard);
        verify(transferRepository).save(any(Transfer.class));
    }

    @Test
    void createTransfer_ShouldThrow_WhenFromCardNotFound() {
        when(cardRepository.findById(fromCardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.createTransfer(userId, transferRequestDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Card of sender not found");
    }

    @Test
    void createTransfer_ShouldThrow_WhenToCardNotFound() {
        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.createTransfer(userId, transferRequestDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Card of recipient not found");
    }

    @Test
    void createTransfer_ShouldThrow_WhenCardsNotOwnedByUser() {
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        fromCard.setOwner(anotherUser);

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));

        assertThatThrownBy(() -> transferService.createTransfer(userId, transferRequestDto))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("You do not own one of the cards involved in the transfer");
    }

    @Test
    void createTransfer_ShouldThrow_WhenCardInactive() {
        fromCard.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));

        assertThatThrownBy(() -> transferService.createTransfer(userId, transferRequestDto))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("All cards involved in the transfer must be active");
    }

    @Test
    void createTransfer_ShouldThrow_WhenInsufficientBalance() {
        fromCard.setBalance(new BigDecimal("100.00"));

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));

        assertThatThrownBy(() -> transferService.createTransfer(userId, transferRequestDto))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Insufficient balance on the sender's card");
    }

    @Test
    void getTransferHistory_ShouldReturnList_WhenCardExists() {
        when(cardRepository.existsById(fromCardId)).thenReturn(true);

        List<Transfer> transfers = List.of(transfer);
        when(transferRepository.findByFromCard_IdOrToCard_Id(fromCardId, fromCardId)).thenReturn(transfers);
        when(transferMapper.toDto(transfer)).thenReturn(transferResponseDto);

        List<TransferResponseDto> result = transferService.getTransferHistory(fromCardId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(transferResponseDto);
    }

    @Test
    void getTransferHistory_ShouldThrow_WhenCardNotFound() {
        when(cardRepository.existsById(fromCardId)).thenReturn(false);

        assertThatThrownBy(() -> transferService.getTransferHistory(fromCardId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Card not found: " + fromCardId);
    }
}
