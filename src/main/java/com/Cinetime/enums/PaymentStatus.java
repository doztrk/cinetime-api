package com.Cinetime.enums;

public enum PaymentStatus {
    PENDING(0),
    SUCCESS(1),
    FAILED(2);


    private final int value;

    PaymentStatus(int value) {
        this.value = value;
    }

    public static PaymentStatus fromValue(int value) {
        for (PaymentStatus status : PaymentStatus.values()) {
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


