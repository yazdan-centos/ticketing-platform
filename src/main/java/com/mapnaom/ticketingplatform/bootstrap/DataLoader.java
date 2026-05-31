package com.mapnaom.ticketingplatform.bootstrap;

import com.mapnaom.ticketingplatform.mapper.SlaContractMapper;
import com.mapnaom.ticketingplatform.model.*;
import com.mapnaom.ticketingplatform.model.enums.Priority;
import com.mapnaom.ticketingplatform.model.enums.TicketStatus;
import com.mapnaom.ticketingplatform.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(AppUserRepository userRepository,
                                   TicketRepository ticketRepository,
                                   TeamMemberRepository teamMemberRepository,
                                   SlaContractRepository slaContractRepository,
                                   TeamManagerRepository teamManagerRepository,
                                   SlaContractMapper slaContractMapper) {
        return args -> {

            if (teamMemberRepository.count() == 0 && slaContractRepository.count() == 0) {

                // ------------------------------------------------
                // 1. Create and Save Team Members (1 Manager + 4 Members)
                // ------------------------------------------------
                TeamManager manager = new TeamManager();
                manager.setUsername("Admin");
                manager.setRoles(Collections.singleton("ADMIN"));


                TeamMember john = new TeamMember();
                john.setUsername("John");
                john.setRoles(Collections.singleton("DEVELOPER"));
                manager.addTeamMember(john);


                TeamMember jane = new TeamMember();
                jane.setUsername("Jane");
                jane.setRoles(Collections.singleton("DEVELOPER"));
                manager.addTeamMember(jane);

                TeamMember mike = new TeamMember();
                mike.setUsername("Mike");
                mike.setRoles(Collections.singleton("QA"));
                manager.addTeamMember(mike);

                TeamMember sarah = new TeamMember();
                sarah.setUsername("Sarah");
                sarah.setRoles(Collections.singleton("DEVOPS"));
                manager.addTeamMember(sarah);

                // Save members first to generate IDs
                teamManagerRepository.save(manager);
                // ------------------------------------------------
                // 2. Create and Save SLA Contracts (Total 5)
                // ------------------------------------------------
                SlaContract sla1 = new SlaContract();
                sla1.setContractName("24/7 Critical Support");
                sla1.setResponseTimeHours(1);
                sla1.setIsActive(true);

                SlaContract sla2 = new SlaContract();
                sla2.setContractName("Business Hours High Priority");
                sla2.setResponseTimeHours(4);
                sla2.setIsActive(true);

                SlaContract sla3 = new SlaContract();
                sla3.setContractName("Standard Medium Priority");
                sla3.setResponseTimeHours(8);
                sla3.setIsActive(true);

                // ------------------------------------------------
                // 2.5 Create and Save Customers (Total 5)
                // ------------------------------------------------
                Customer customer1 = new Customer();
                customer1.setUsername("Alice");
                customer1.setRoles(Collections.singleton("CUSTOMER"));

                Customer customer2 = new Customer();
                customer2.setUsername("Bob");
                customer2.setRoles(Collections.singleton("CUSTOMER"));

                Customer customer3 = new Customer();
                customer3.setUsername("Charlie");
                customer3.setRoles(Collections.singleton("CUSTOMER"));

                Customer customer4 = new Customer();
                customer4.setUsername("Diana");
                customer4.setRoles(Collections.singleton("CUSTOMER"));

                Customer customer5 = new Customer();
                customer5.setUsername("Evan");
                customer5.setRoles(Collections.singleton("CUSTOMER"));

                // Save Customers to generate IDs
                userRepository.saveAll(Arrays.asList(customer1, customer2, customer3, customer4, customer5));

                SlaContract sla4 = new SlaContract();
                sla4.setContractName("Low Priority Request");
                sla4.setResponseTimeHours(24);
                sla4.setIsActive(true);

                SlaContract sla5 = new SlaContract();
                sla5.setContractName("Basic Maintenance SLA");
                sla5.setResponseTimeHours(48);
                sla5.setIsActive(false); // Mocking an inactive/expired SLA

                // Save SLA contracts first to generate IDs
                slaContractRepository.saveAll(Arrays.asList(sla1, sla2, sla3, sla4, sla5));

                // ------------------------------------------------
                // 3. Create Mock Tickets (Total 5)
                // ------------------------------------------------

                // Ticket 1 (High Priority)
                Ticket t1 = new Ticket();
                t1.setTitle("Server Down - Urgent");
                t1.setDescription("The main production server is not responding. Customers are affected.");
                t1.setStatus(TicketStatus.ASSIGNED);
                t1.setPriority(Priority.HIGH);
                t1.setCreatedAt(LocalDateTime.now().minusDays(1));
                t1.setSlaContract(sla1);       // Link to Critical SLA
                t1.setCustomer(customer1);     // Link to Customer

                // Ticket 2 (Low Priority)
                Ticket t2 = new Ticket();
                t2.setTitle("Login Issue");
                t2.setDescription("User cannot reset password via the forgot password link.");
                t2.setStatus(TicketStatus.CLOSED);
                t2.setPriority(Priority.LOW);
                t2.setCreatedAt(LocalDateTime.now().minusHours(5));
                t2.setAssignedMember(john);    // Link to John
                t2.setSlaContract(sla4);       // Link to Low Priority SLA
                t2.setCustomer(customer2);     // Link to Customer

                // Ticket 3 (Critical Priority)
                Ticket t3 = new Ticket();
                t3.setTitle("Feature Request: Dark Mode");
                t3.setDescription("It would be nice to have a dark mode option in the settings.");
                t3.setStatus(TicketStatus.ASSIGNED);
                t3.setPriority(Priority.CRITICAL);
                t3.setCreatedAt(LocalDateTime.now());
                t3.setAssignedMember(jane);    // Link to Jane
                t3.setSlaContract(sla2);       // Link to High Priority SLA
                t3.setCustomer(customer3);     // Link to Customer

                // Ticket 4 (Low Priority)
                Ticket t4 = new Ticket();
                t4.setTitle("UI Bug on Dashboard");
                t4.setDescription("The charts overlap on mobile view.");
                t4.setStatus(TicketStatus.IN_PROGRESS);
                t4.setPriority(Priority.LOW);
                t4.setCreatedAt(LocalDateTime.now().minusWeeks(1));
                t4.setAssignedMember(mike);    // Link to Mike
                t4.setSlaContract(sla3);       // Link to Standard SLA
                t4.setCustomer(customer4);     // Link to Customer

                // Ticket 5 (Medium Priority)
                Ticket t5 = new Ticket();
                t5.setTitle("Database Backup Failure");
                t5.setDescription("Nightly backup script threw an error code 500.");
                t5.setStatus(TicketStatus.ASSIGNED);
                t5.setPriority(Priority.MEDIUM);
                t5.setCreatedAt(LocalDateTime.now().minusMinutes(30));
                t5.setAssignedMember(sarah);   // Link to Sarah
                t5.setSlaContract(sla5);       // Link to Basic Maintenance SLA
                t5.setCustomer(customer5);     // Link to Customer

                // Save all Tickets
                ticketRepository.saveAll(Arrays.asList(t1, t2, t3, t4, t5));

                System.out.println("Sample Data Loaded: 5 Team Members (1 Manager), 5 Customers, 5 SLA Contracts, and 5 Tickets created.");
            }
        };
    }
}
