package com.mapnaom.ticketingplatform.service.impl;

import com.mapnaom.ticketingplatform.dto.ticket.TicketResponse;
import com.mapnaom.ticketingplatform.dto.ticket.TicketUpdateRequest;
import com.mapnaom.ticketingplatform.mapper.TicketCustomerMapper;
import com.mapnaom.ticketingplatform.mapper.TicketMapper;
import com.mapnaom.ticketingplatform.model.TeamMember;
import com.mapnaom.ticketingplatform.model.Ticket;
import com.mapnaom.ticketingplatform.model.TicketStatusHistory;
import com.mapnaom.ticketingplatform.model.enums.TicketStatus;
import com.mapnaom.ticketingplatform.repository.AppUserRepository;
import com.mapnaom.ticketingplatform.repository.SlaContractRepository;
import com.mapnaom.ticketingplatform.repository.TeamMemberRepository;
import com.mapnaom.ticketingplatform.repository.TicketAttachmentRepository;
import com.mapnaom.ticketingplatform.repository.TicketRepository;
import com.mapnaom.ticketingplatform.repository.TicketStatusHistoryRepository;
import com.mapnaom.ticketingplatform.service.AccessChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {
    @Mock private TicketRepository ticketRepository;
    @Mock private SlaContractRepository slaContractRepository;
    @Mock private TeamMemberRepository teamMemberRepository;
    @Mock private AppUserRepository appUserRepository;
    @Mock private TicketStatusHistoryRepository ticketStatusHistoryRepository;
    @Mock private TicketCustomerMapper ticketCustomerMapper;
    @Mock private TicketAttachmentRepository ticketAttachmentRepository;
    @Mock private AccessChecker access;

    private TicketServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TicketServiceImpl(
                ticketRepository,
                slaContractRepository,
                teamMemberRepository,
                appUserRepository,
                ticketStatusHistoryRepository,
                new TicketMapper(),
                ticketCustomerMapper,
                ticketAttachmentRepository,
                access);
    }

    @Test
    void updateClearsAssigneeWhenStatusRevertsToUnallocated() {
        TeamMember assignee = teamMember(20L);
        TeamMember actor = teamMember(30L);
        Ticket ticket = new Ticket();
        ticket.setId(10L);
        ticket.setStatus(TicketStatus.ASSIGNED);
        ticket.setAssignedMember(assignee);

        TicketUpdateRequest request = new TicketUpdateRequest();
        request.setStatus(TicketStatus.UNALLOCATED);
        request.setStatusNote("Returned to the unallocated queue");

        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(appUserRepository.findById(actor.getId())).thenReturn(Optional.of(actor));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = service.update(ticket.getId(), request, actor.getId());

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.UNALLOCATED);
        assertThat(ticket.getAssignedMember()).isNull();
        assertThat(response.getAssignedMemberId()).isNull();

        ArgumentCaptor<TicketStatusHistory> historyCaptor = ArgumentCaptor.forClass(TicketStatusHistory.class);
        verify(ticketStatusHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getOldStatus()).isEqualTo(TicketStatus.ASSIGNED);
        assertThat(historyCaptor.getValue().getNewStatus()).isEqualTo(TicketStatus.UNALLOCATED);
    }

    private TeamMember teamMember(Long id) {
        TeamMember member = new TeamMember();
        member.setId(id);
        member.setUsername("member-" + id);
        return member;
    }
}
