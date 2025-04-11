package com.Cinetime.payload.mappers;

import com.Cinetime.entity.Ticket;
import com.Cinetime.payload.dto.TicketDto;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {
    public TicketDto toDto(Ticket ticket) {
        String seat = ticket.getSeatLetter() + ticket.getSeatNumber();

        return new TicketDto(
                ticket.getId(),
                ticket.getMovie().getTitle(),
                ticket.getShowtime().getStartTime().toString(),
                ticket.getHall().getName(),
                seat,
                ticket.getPrice()
        );
    }
}
