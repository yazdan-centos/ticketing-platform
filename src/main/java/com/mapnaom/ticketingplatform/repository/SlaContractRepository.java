package com.mapnaom.ticketingplatform.repository;

import com.mapnaom.ticketingplatform.model.SlaContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlaContractRepository extends JpaRepository<SlaContract, Long> {
}