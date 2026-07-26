package com.mapnaom.ticketingplatform.repository;

import com.mapnaom.ticketingplatform.model.SlaContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SlaContractRepository extends JpaRepository<SlaContract, Long>, JpaSpecificationExecutor<SlaContract> {
    @Query("""
            select s from SlaContract s
            join fetch s.customer c
            where s.isActive = true
              and (:searchKey = ''
                or lower(s.contractName) like lower(concat('%', :searchKey, '%'))
                or lower(c.firstName) like lower(concat('%', :searchKey, '%'))
                or lower(c.lastName) like lower(concat('%', :searchKey, '%'))
                or lower(c.companyName) like lower(concat('%', :searchKey, '%')))
            order by s.contractName
            """)
    List<SlaContract> findActiveOptions(@Param("searchKey") String searchKey);
}
