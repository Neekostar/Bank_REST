package org.example.neekostar.bank.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.neekostar.bank.entity.Card;
import org.example.neekostar.bank.entity.CardStatus;
import org.example.neekostar.bank.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    Optional<Card> findById(UUID cardId);

    Page<Card> findByOwner_IdAndStatus(UUID userId, CardStatus status, Pageable pageable);

    Page<Card> findByOwnerId(UUID userId, Pageable pageable);
}
