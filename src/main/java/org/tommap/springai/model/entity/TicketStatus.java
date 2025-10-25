package org.tommap.springai.model.entity;

import lombok.Getter;

@Getter
public enum TicketStatus {
    OPEN("open"),
    IN_PROGRESS("in_progress"),
    CLOSED("closed");

    private final String value;

    TicketStatus(String value) {
        this.value = value;
    }
}
