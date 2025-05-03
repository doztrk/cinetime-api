package com.Cinetime.payload.business;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatInfo {

    @NotNull(message = "Seat letter cannot be null")
    @Pattern(regexp = "^[A-Z]$", message = "Seat letter must be an uppercase letter")
    private String seatLetter;

    @NotNull(message = "Seat number cannot be null")
    @Min(value = 1, message = "Seat number must be at least 1")
    private Integer seatNumber;

    @NotNull(message = "Price cannot be null")
    @Min(value = 0, message = "Price must be a positive number")
    private Double price;


    public String getFullSeatName() {
        return seatLetter + seatNumber;
    }


    public boolean isValidSeat() {
        return seatLetter != null && seatNumber != null &&
                seatLetter.matches("^[A-Z]$") && seatNumber > 0;
    }
}
