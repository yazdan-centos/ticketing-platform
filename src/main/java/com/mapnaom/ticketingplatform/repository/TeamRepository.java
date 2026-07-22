package com.mapnaom.ticketingplatform.repository;

import com.mapnaom.ticketingplatform.model.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Page<Team> findByActiveTrue(Pageable pageable);

    Optional<Team> findByIdAndActiveTrue(Long id);

    boolean existsByNameIgnoreCaseAndActiveTrue(String name);
}
