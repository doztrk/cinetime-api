package com.Cinetime.payload.mappers;

import com.Cinetime.entity.Movie;
import com.Cinetime.entity.Showtime;
import com.Cinetime.entity.Ticket;
import com.Cinetime.entity.User;
import com.Cinetime.enums.TicketStatus;
import com.Cinetime.payload.dto.TicketDto;
import com.Cinetime.payload.dto.user.TicketRequestDto;
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

    public Ticket mapRequestToTicket(TicketRequestDto request, Movie movie, Showtime showtime, User user) {
        Ticket ticket = new Ticket();
        ticket.setMovie(movie);
        ticket.setShowtime(showtime);
        ticket.setSeatLetter(request.getSeatLetter());
        ticket.setSeatNumber(request.getSeatNumber());
        ticket.setUser(user);
        ticket.setStatus(TicketStatus.RESERVED);
        ticket.setPrice(15.0);
        ticket.setHall(showtime.getHall());
        ticket.setPayment(null);
        return ticket;
    }
}
