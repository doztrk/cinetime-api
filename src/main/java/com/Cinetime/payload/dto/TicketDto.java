package com.Cinetime.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketDto {
    private Long id;
    private String movieName;
    private String showtimeStart;
    private String hallName;
    private String seat;
    private Double price;
}
