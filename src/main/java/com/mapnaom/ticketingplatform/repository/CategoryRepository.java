package com.mapnaom.ticketingplatform.repository;

import com.mapnaom.ticketingplatform.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * Check if a category exists with the given name.
     * @param name the category name
     * @return true if exists, false otherwise
     */
    boolean existsByName(String name);
    
    /**
     * Find a category by its name.
     * @param name the category name
     * @return Optional containing the category if found
     */
    Optional<Category> findByName(String name);
}
