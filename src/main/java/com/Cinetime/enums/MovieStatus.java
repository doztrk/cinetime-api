package com.Cinetime.enums;


public enum MovieStatus {
    COMING_SOON(0),
    IN_THEATERS(1),
    ENDED(2);

    private final int value;

    MovieStatus(int value) {
        this.value = value;
    }

    public static MovieStatus fromValue(int value) {
        for (MovieStatus status : MovieStatus.values()) {
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
