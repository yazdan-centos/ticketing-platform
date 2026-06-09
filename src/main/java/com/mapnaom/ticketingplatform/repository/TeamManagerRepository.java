package com.mapnaom.ticketingplatform.repository;

import com.mapnaom.ticketingplatform.model.TeamManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamManagerRepository extends JpaRepository<TeamManager, Long>, JpaSpecificationExecutor<TeamManager> {
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}