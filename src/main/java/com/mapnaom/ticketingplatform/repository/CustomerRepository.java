package com.mapnaom.ticketingplatform.repository;

import com.mapnaom.ticketingplatform.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<Customer> findByUsername(String username);

    @Query("""
            select distinct c from Customer c
            join c.slaContracts s
            where s.id = :slaContractId
              and s.isActive = true
              and (:searchKey = ''
                or lower(c.firstName) like lower(concat('%', :searchKey, '%'))
                or lower(c.lastName) like lower(concat('%', :searchKey, '%'))
                or lower(c.username) like lower(concat('%', :searchKey, '%'))
                or lower(c.email) like lower(concat('%', :searchKey, '%'))
                or lower(c.companyName) like lower(concat('%', :searchKey, '%')))
            order by c.firstName, c.lastName
            """)
    List<Customer> findOptionsBySlaContract(
            @Param("slaContractId") Long slaContractId,
            @Param("searchKey") String searchKey);
}
