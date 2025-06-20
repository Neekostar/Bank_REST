package org.example.neekostar.bank.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
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
import org.example.neekostar.bank.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransferServiceImpl implements TransferService {

    private final CardRepository cardRepository;
    private final TransferRepository transferRepository;
    private final UserRepository userRepository;
    private final TransferMapper transferMapper;

    @Autowired
    public TransferServiceImpl(CardRepository cardRepository,
                               TransferRepository transferRepository,
                               UserRepository userRepository,
                               TransferMapper transferMapper) {
        this.cardRepository = cardRepository;
        this.transferRepository = transferRepository;
        this.userRepository = userRepository;
        this.transferMapper = transferMapper;
    }

    @Override
    @Transactional
    public TransferResponseDto createTransfer(UUID userId, TransferRequestDto request) {
        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new NotFoundException("Card of sender not found"));

        Card toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new NotFoundException("Card of recipient not found"));

        if (!fromCard.getOwner().getId().equals(userId) || !toCard.getOwner().getId().equals(userId)) {
            throw new InvalidArgumentException("You do not own one of the cards involved in the transfer");
        }

        if (fromCard.getStatus() != CardStatus.ACTIVE || toCard.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidArgumentException("All cards involved in the transfer must be active");
        }

        BigDecimal amount = request.getAmount();
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new InvalidArgumentException("Insufficient balance on the sender's card");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        Transfer transfer = new Transfer();
        transfer.setFromCard(fromCard);
        transfer.setToCard(toCard);
        transfer.setAmount(amount);

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
        transferRepository.save(transfer);

        return transferMapper.toDto(transfer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferResponseDto> getTransferHistory(UUID cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new NotFoundException("Card not found: " + cardId);
        }

        return transferRepository.findByFromCard_IdOrToCard_Id(cardId, cardId).stream()
                .map(transferMapper::toDto)
                .toList();
    }
}
