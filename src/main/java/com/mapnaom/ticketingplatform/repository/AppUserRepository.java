package com.mapnaom.ticketingplatform.repository;

import com.mapnaom.ticketingplatform.model.AppUser;
import com.mapnaom.ticketingplatform.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);

    @Query(value = """
            SELECT EXISTS (
                SELECT 1 FROM tickets WHERE customer_id = :userId OR assigned_member_id = :userId
                UNION ALL SELECT 1 FROM sla_contracts WHERE customer_id = :userId
                UNION ALL SELECT 1 FROM tasks WHERE assigned_member_id = :userId OR created_by_id = :userId
                UNION ALL SELECT 1 FROM meetings WHERE organizer_id = :userId
                UNION ALL SELECT 1 FROM meeting_participants WHERE user_id = :userId
                UNION ALL SELECT 1 FROM meeting_notes WHERE author_id = :userId
                UNION ALL SELECT 1 FROM agenda_items WHERE presenter_id = :userId
                UNION ALL SELECT 1 FROM team_memberships WHERE user_id = :userId
                UNION ALL SELECT 1 FROM ticket_messages WHERE sender_id = :userId
                UNION ALL SELECT 1 FROM ticket_attachments WHERE uploaded_by_id = :userId
                UNION ALL SELECT 1 FROM ticket_status_histories WHERE changed_by_id = :userId
                UNION ALL SELECT 1 FROM app_users WHERE manager_id = :userId
            )
            """, nativeQuery = true)
    boolean hasAssociatedData(@Param("userId") Long userId);

    // Custom query to find only TeamMembers, ignoring Customers/Managers
    // This leverages the @DiscriminatorValue("MEMBER")
    @Query("SELECT m FROM TeamMember m")
    List<TeamMember> findAllTeamMembers();
}
