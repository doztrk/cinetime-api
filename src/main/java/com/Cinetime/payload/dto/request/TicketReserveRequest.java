package com.Cinetime.payload.dto.request;

import com.Cinetime.payload.business.SeatInfo;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketReserveRequest {


    @NotNull(message = "Showtime ID cannot be null")
    private Long showtimeId;


    @NotNull(message = "Hall name cannot be null")
    private String hall;

    private List<SeatInfo> seatInfos;


}
