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
public class TicketPurchaseGuestRequest {

    @NotNull(message = "Movie name cannot be null")
    private String movieName;
    @NotNull(message = "Showtime ID cannot be null")
    private Long showtimeId;
    @NotNull(message = "Ticket price cannot be null")
    private Double ticketPrice;
    @NotNull(message = "Seats cannot be null")
    private List<SeatInfo> seatInfos;
    @NotNull
    private GuestInfoRequest anonymousUser;
}
