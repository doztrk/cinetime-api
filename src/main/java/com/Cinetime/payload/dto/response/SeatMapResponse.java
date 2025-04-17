package com.Cinetime.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatMapResponse {
    private int rowCount;
    private int columnCount;
    private List<String> reservedSeats;
}
