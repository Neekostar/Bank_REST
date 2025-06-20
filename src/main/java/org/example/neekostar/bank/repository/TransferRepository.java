package org.example.neekostar.bank.repository;

import java.util.List;
import java.util.UUID;
import org.example.neekostar.bank.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    List<Transfer> findByFromCard_IdOrToCard_Id(UUID fromCardId, UUID toCardId);
}
