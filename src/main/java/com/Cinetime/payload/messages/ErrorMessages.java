package com.Cinetime.payload.messages;

public class ErrorMessages {
    public static final String DUPLICATE_USER_PROPERTIES = "User with this email or phone number already exists";
    public static final String HALL_NOT_FOUND = "Hall not found with the provided ID";
    public static final String SHOWTIME_NOT_FOUND = "Showtime not found with the provided ID";
    public static final String BUILTIN_USER_UPDATE = "Built-in users cannot be updated";
    public static final String BUILTIN_USER_DELETE = "Built-in users cannot be updated";

    public static final String USER_HAS_UNUSED_TICKETS = "User has unused tickets";
    public static final String INVALID_PASSWORD = "Invalid password";
    public static final String CINEMA_NOT_FOUND = "Cinema not found with the provided ID";
    public static final String MOVIE_NOT_FOUND = "Movie not found";
    public static final String MOVIES_NOT_FOUND = "Movies not found";
    public static final String USER_NOT_FOUND_WITH_ID = "User not found with the provided ID";
    public static final String UNAUTHORIZED_USER_UPDATE = "Employee type of user trying to update non-member user";
    public static final String DUPLICATE_EMAIL = "User with this email already exists";
    public static final String DUPLICATE_PHONE_NUMBER = "User with this phone number already exists";
    public static final String AUTHENTICATION_NOT_FOUND = "Authentication not found with the provided Phone Number, this indicates a system error";
    public static final String PAYMENT_ERROR = "Unexpected error occured during payment" ;
    public static final String NO_SEAT_SPECIFIED = "No seats specified for the given ticket request";

    public static final String SEATS_ARE_OCCUPIED = "The following seats are already occupied:  ";
}
