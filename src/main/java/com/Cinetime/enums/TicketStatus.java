package com.Cinetime.enums;

public enum TicketStatus {
    RESERVED(0),
    PAID(1),
    CANCELLED(2);

    private final int value;

    TicketStatus(int value) {
        this.value = value;
    }

    public static TicketStatus fromValue(int value) {
        for (TicketStatus status : TicketStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status value: " + value);
    }

    public int getValue() {
        return value;
    }


}
