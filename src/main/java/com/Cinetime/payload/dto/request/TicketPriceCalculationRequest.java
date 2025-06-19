package com.Cinetime.payload.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketPriceCalculationRequest {


    @NotNull(message = "Showtime ID cannot be null")
    private Long showtimeId;

    @NotEmpty(message = "Seats cannot be empty")
    @Valid
    private List<SeatPosition> seats;

    /**
     * Inner class to represent just the seat position without price
     * (since we're calculating the price on the server)
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SeatPosition {
        @NotNull(message = "Seat letter cannot be null")
        private String seatLetter;

        @NotNull(message = "Seat number cannot be null")
        private Integer seatNumber;
    }
}
