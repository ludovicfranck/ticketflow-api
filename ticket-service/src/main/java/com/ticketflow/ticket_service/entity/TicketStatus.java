package com.ticketflow.ticket_service.entity;

import lombok.Getter;

@Getter
public enum TicketStatus {
    OPEN, IN_PROGRESS, RESOLVED, CLOSED;
    public boolean canTransitionTo(TicketStatus next) {
        return switch (this) {
            case OPEN       -> next == IN_PROGRESS;
            case IN_PROGRESS-> next == RESOLVED ;
            case RESOLVED   -> next == CLOSED ;
            case CLOSED     -> false;
        };
    }
}
