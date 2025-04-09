package com.Cinetime.helpers;

import com.Cinetime.entity.Showtime;
import com.Cinetime.entity.Ticket;
import com.Cinetime.entity.User;
import com.Cinetime.enums.TicketStatus;
import com.Cinetime.repo.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TicketHelper {

    private final TicketRepository ticketRepository;


    /*public boolean canDeleteUser(User user) {

        List<Ticket> allTickets = ticketRepository.findAllTicketsByUser(user);

        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        return allTickets.stream()
                .noneMatch(ticket -> {
                    Showtime showtime = ticket.getShowtime();
                    boolean isFutureShowtime = showtime.getDate().isAfter(currentDate) ||
                            (showtime.getDate().isEqual(currentDate) &&
                                    showtime.getStartTime().isAfter(currentTime));

                    boolean isActiveStatus = ticket.getStatus() == TicketStatus.RESERVED ||
                            ticket.getStatus() == TicketStatus.PAID;

                    return isFutureShowtime && isActiveStatus;
                });

    }*/
    public boolean canDeleteUser(User user) {
        List<Ticket> allTickets = ticketRepository.findAllTicketsByUser(user);

        // Add debugging
        System.out.println("User ID: " + user.getId());
        System.out.println("Total tickets found: " + allTickets.size());

        // If there are no tickets, the user can be deleted
        if (allTickets.isEmpty()) {
            return true;
        }

        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        // Check each ticket and log
        for (Ticket ticket : allTickets) {
            Showtime showtime = ticket.getShowtime();
            boolean isFutureShowtime = showtime.getDate().isAfter(currentDate) ||
                    (showtime.getDate().isEqual(currentDate) &&
                            showtime.getStartTime().isAfter(currentTime));

            boolean isActiveStatus = ticket.getStatus() == TicketStatus.RESERVED ||
                    ticket.getStatus() == TicketStatus.PAID;

            boolean isProblematic = isFutureShowtime && isActiveStatus;

            System.out.println("Ticket ID: " + ticket.getId() +
                    ", Future: " + isFutureShowtime +
                    ", Active: " + isActiveStatus +
                    ", Problematic: " + isProblematic);

            if (isProblematic) {
                return false;  // User has future active tickets
            }
        }

        return true;  // No problematic tickets found
    }

}
