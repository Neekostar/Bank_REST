package org.example.neekostar.bank.repository;

import java.util.List;
import java.util.UUID;
import org.example.neekostar.bank.entity.CardBlockRequest;
import org.example.neekostar.bank.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardBlockRequestRepository extends JpaRepository<CardBlockRequest, UUID> {

    List<CardBlockRequest> findByStatus(RequestStatus status);
}
