package com.Cinetime.payload.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketPurchaseRequest {

    private String movieName;
    private String cinema;
    private String hall;
    private Long showtimeId;
    private Integer count;
    private List<SeatRequest> seats;


}
