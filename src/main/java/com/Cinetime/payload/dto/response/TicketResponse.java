package com.Cinetime.payload.dto.response;

import com.Cinetime.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketResponse {

    private Long id;
    private String seatLetter;
    private Integer seatNumber;
    private Double price;
    private String movieName;
    private Long showTimeId;
    private LocalDate showTimeDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String ticketOwnerNameSurname;
    private String hallName;
    private String cinemaName;
    private String cinemaAdress;
    private LocalDateTime createdAt;
    private TicketStatus status;


}
