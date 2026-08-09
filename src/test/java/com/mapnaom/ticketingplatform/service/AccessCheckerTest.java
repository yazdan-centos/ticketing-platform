package com.mapnaom.ticketingplatform.service;

import com.mapnaom.ticketingplatform.model.enums.AccessScope;
import com.mapnaom.ticketingplatform.model.AppUserDetails;
import com.mapnaom.ticketingplatform.model.Customer;
import com.mapnaom.ticketingplatform.model.Meeting;
import com.mapnaom.ticketingplatform.model.Team;
import com.mapnaom.ticketingplatform.model.TeamMember;
import com.mapnaom.ticketingplatform.model.Ticket;
import com.mapnaom.ticketingplatform.repository.MeetingParticipantRepository;
import com.mapnaom.ticketingplatform.repository.TeamMembershipRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessCheckerTest {
    @Mock private TeamMembershipRepository membershipRepository;
    @Mock private MeetingParticipantRepository participantRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void customerOwnScopeDoesNotExposeAnotherCustomersTicket() {
        Customer viewer = customer(1L, "viewer");
        authenticate(viewer, Map.of("TICKET", AccessScope.OWN));
        Ticket ticket = new Ticket();
        ticket.setCustomer(customer(2L, "other"));

        AccessChecker access = checker();

        assertThat(access.canSee("TICKET", ticket)).isFalse();
        assertThatThrownBy(() -> access.requireCanSee("TICKET", ticket))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void teamScopeAllowsOnlyMeetingsInTheAuthenticatedUsersTeam() {
        TeamMember viewer = member(10L, "viewer");
        authenticate(viewer, Map.of("MEETING", AccessScope.TEAM));
        Team team = new Team();
        team.setId(20L);
        Meeting meeting = new Meeting();
        meeting.setTeam(team);
        when(membershipRepository.existsByTeamIdAndUserId(20L, 10L)).thenReturn(false, true);

        AccessChecker access = checker();

        assertThat(access.canSee("MEETING", meeting)).isFalse();
        assertThat(access.canSee("MEETING", meeting)).isTrue();
    }

    @Test
    void allScopeAllowsManagerAccessWithoutTeamMembership() {
        TeamMember manager = member(30L, "manager");
        authenticate(manager, Map.of("TASK", AccessScope.ALL));

        assertThat(checker().canSee("TASK", new com.mapnaom.ticketingplatform.model.Task())).isTrue();
    }

    private AccessChecker checker() {
        return new AccessChecker(membershipRepository, participantRepository);
    }

    private void authenticate(com.mapnaom.ticketingplatform.model.AppUser user,
                              Map<String, AccessScope> scopes) {
        AppUserDetails principal = new AppUserDetails(user, Set.of(), scopes);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private Customer customer(Long id, String username) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setUsername(username);
        customer.setPassword("password");
        return customer;
    }

    private TeamMember member(Long id, String username) {
        TeamMember member = new TeamMember();
        member.setId(id);
        member.setUsername(username);
        member.setPassword("password");
        return member;
    }
}
