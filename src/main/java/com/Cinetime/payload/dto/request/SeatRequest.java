package com.Cinetime.payload.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatRequest {
    private String seatLetter;
    private Integer seatNumber;
}
