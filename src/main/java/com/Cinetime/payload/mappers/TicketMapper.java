package com.Cinetime.payload.mappers;

import com.Cinetime.entity.*;
import com.Cinetime.payload.dto.response.TicketResponse;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class TicketMapper {


    public TicketResponse mapTicketToTicketResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .seatLetter(ticket.getSeatLetter())
                .seatNumber(ticket.getSeatNumber())
                .price(ticket.getPrice())
                .movieName(ticket.getShowtime().getMovie().getTitle())
                .showTimeId(ticket.getShowtime().getId())
                .showTimeDate(ticket.getShowtime().getDate())
                .startTime(ticket.getShowtime().getStartTime())
                .endTime(ticket.getShowtime().getEndTime())
                .ticketOwnerNameSurname(ticket.getUser().getFirstname() + " " + ticket.getUser().getLastname())
                .hallName(ticket.getShowtime().getHall().getName())
                .cinemaName(ticket.getShowtime().getHall().getCinema().getName())
                .cinemaAdress(ticket.getShowtime().getHall().getCinema().getAddress())
                .createdAt(ticket.getCreatedAt())
                .build();
    }
}
