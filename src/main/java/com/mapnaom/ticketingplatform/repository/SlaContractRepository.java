package com.mapnaom.ticketingmanagerserver.repository;

import com.mapnaom.ticketingmanagerserver.model.SlaContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlaContractRepository extends JpaRepository<SlaContract, Long> {
}