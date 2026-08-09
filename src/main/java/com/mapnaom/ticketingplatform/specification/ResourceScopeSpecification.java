package com.mapnaom.ticketingplatform.specification;

import com.mapnaom.ticketingplatform.model.enums.AccessScope;
import com.mapnaom.ticketingplatform.model.Meeting;
import com.mapnaom.ticketingplatform.model.MeetingParticipant;
import com.mapnaom.ticketingplatform.model.Task;
import com.mapnaom.ticketingplatform.model.TeamMembership;
import com.mapnaom.ticketingplatform.model.Ticket;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public final class ResourceScopeSpecification {
    private ResourceScopeSpecification() {
    }

    public static Specification<Ticket> tickets(AccessScope scope, Long userId, Long managerId) {
        return (root, query, cb) -> switch (scope) {
            case ALL -> cb.conjunction();
            case OWN -> cb.equal(root.get("customer").get("id"), userId);
            case ASSIGNED -> cb.equal(root.get("assignedMember").get("id"), userId);
            case TEAM -> {
                var assignee = root.join("assignedMember", JoinType.LEFT);
                var sharedTeam = query.subquery(Integer.class);
                var viewerMembership = sharedTeam.from(TeamMembership.class);
                var assigneeMembership = sharedTeam.from(TeamMembership.class);
                sharedTeam.select(cb.literal(1)).where(
                        cb.equal(viewerMembership.get("user").get("id"), userId),
                        cb.equal(assigneeMembership.get("user").get("id"), assignee.get("id")),
                        cb.equal(viewerMembership.get("team").get("id"), assigneeMembership.get("team").get("id")));

                var legacyManagerScope = managerId == null
                        ? cb.disjunction()
                        : cb.equal(assignee.join("manager", JoinType.LEFT).get("id"), managerId);
                yield cb.or(
                        cb.equal(assignee.get("id"), userId),
                        legacyManagerScope,
                        cb.exists(sharedTeam));
            }
            case NONE -> cb.disjunction();
        };
    }

    public static Specification<Meeting> meetings(AccessScope scope, Long userId) {
        return (root, query, cb) -> switch (scope) {
            case ALL -> cb.conjunction();
            case OWN -> cb.equal(root.get("organizer").get("id"), userId);
            case ASSIGNED -> {
                var participant = query.subquery(Integer.class);
                var participantRoot = participant.from(MeetingParticipant.class);
                participant.select(cb.literal(1)).where(
                        cb.equal(participantRoot.get("meeting").get("id"), root.get("id")),
                        cb.equal(participantRoot.get("user").get("id"), userId));
                yield cb.exists(participant);
            }
            case TEAM -> {
                var membership = query.subquery(Integer.class);
                var membershipRoot = membership.from(TeamMembership.class);
                membership.select(cb.literal(1)).where(
                        cb.equal(membershipRoot.get("team").get("id"), root.get("team").get("id")),
                        cb.equal(membershipRoot.get("user").get("id"), userId));
                yield cb.exists(membership);
            }
            case NONE -> cb.disjunction();
        };
    }

    public static Specification<Task> tasks(AccessScope scope, Long userId, Long managerId) {
        return (root, query, cb) -> switch (scope) {
            case ALL -> cb.conjunction();
            case OWN -> cb.equal(root.get("createdBy").get("id"), userId);
            case ASSIGNED -> cb.equal(root.get("assignedMember").get("id"), userId);
            case TEAM -> {
                var meeting = root.join("meeting", JoinType.LEFT);
                var team = meeting.join("team", JoinType.LEFT);
                var assignee = root.join("assignedMember", JoinType.LEFT);
                var meetingMembership = query.subquery(Integer.class);
                var membershipRoot = meetingMembership.from(TeamMembership.class);
                meetingMembership.select(cb.literal(1)).where(
                        cb.equal(membershipRoot.get("team").get("id"), team.get("id")),
                        cb.equal(membershipRoot.get("user").get("id"), userId));

                var sharedTeam = query.subquery(Integer.class);
                var viewerMembership = sharedTeam.from(TeamMembership.class);
                var assigneeMembership = sharedTeam.from(TeamMembership.class);
                sharedTeam.select(cb.literal(1)).where(
                        cb.equal(viewerMembership.get("user").get("id"), userId),
                        cb.equal(assigneeMembership.get("user").get("id"), assignee.get("id")),
                        cb.equal(viewerMembership.get("team").get("id"), assigneeMembership.get("team").get("id")));

                var legacyManagerScope = managerId == null
                        ? cb.disjunction()
                        : cb.equal(assignee.join("manager", JoinType.LEFT).get("id"), managerId);
                yield cb.or(
                        cb.exists(meetingMembership),
                        cb.and(cb.isNull(meeting.get("id")), cb.or(
                                cb.exists(sharedTeam), legacyManagerScope,
                                cb.equal(assignee.get("id"), userId))));
            }
            case NONE -> cb.disjunction();
        };
    }
}
