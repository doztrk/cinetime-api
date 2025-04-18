package com.Cinetime.controller;

import com.Cinetime.entity.Showtime;
import com.Cinetime.entity.Ticket;
import com.Cinetime.exception.ResourceNotFoundException;
import com.Cinetime.payload.response.SeatMapResponse;
import com.Cinetime.repo.ShowtimeRepository;
import com.Cinetime.repo.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {
    private final TicketRepository ticketRepository;
    private final ShowtimeRepository showtimeRepository;

    //SeatMap for user
    @GetMapping("/{showtimeId}")
    public ResponseEntity<SeatMapResponse> getSeatMap(@PathVariable Long showtimeId) {

        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found"));

        List<Ticket> reservedTickets = ticketRepository.findByShowtime(showtime);

        List<String> reservedSeats = reservedTickets.stream()
                .map(t -> t.getSeatLetter() + t.getSeatNumber())
                .toList();

        int rowCount = showtime.getHall().getRowCount();
        int columnCount = showtime.getHall().getColumnCount();

        SeatMapResponse response = new SeatMapResponse(rowCount, columnCount, reservedSeats);
        return ResponseEntity.ok(response);
    }
}
