package com.Cinetime.service;

import com.Cinetime.entity.Movie;
import com.Cinetime.entity.Showtime;
import com.Cinetime.entity.Ticket;
import com.Cinetime.entity.User;
import com.Cinetime.enums.TicketStatus;
import com.Cinetime.exception.ConflictException;
import com.Cinetime.exception.ResourceNotFoundException;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.payload.dto.TicketDto;
import com.Cinetime.payload.dto.TicketPurchaseRequest;
import com.Cinetime.payload.dto.user.TicketRequestDto;
import com.Cinetime.payload.mappers.TicketMapper;
import com.Cinetime.repo.MovieRepository;
import com.Cinetime.repo.ShowtimeRepository;
import com.Cinetime.repo.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final PageableHelper pageableHelper;
    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;

    //T01 Return movies that an authenticated user bought and haven't used yet
    public Page<TicketDto> getCurrentTickets(User user, int page, int size, String sort, String type) {
        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);
        Page<Ticket> tickets = ticketRepository
                .findByUserAndStatus(user, TicketStatus.RESERVED, pageable);

        return tickets.map(ticketMapper::toDto);
    }

    //T02 Return movies that an authenticated user bought and used
    public Page<TicketDto> getPassedTickets(User user, int page, int size, String sort, String type) {
        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);
        Page<Ticket> tickets = ticketRepository
                .findByUserAndStatus(user, TicketStatus.USED, pageable);

        return tickets.map(ticketMapper::toDto);
    }

    //T03 reserve movie ticket
    public TicketDto reserveTicket(TicketRequestDto request, User user) {

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));

        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found"));

        // aynı koltuk daha önce rezerve edilmiş mi
        Optional<Ticket> existing = ticketRepository.findByShowtimeAndSeatLetterAndSeatNumber(
                showtime, request.getSeatLetter(), request.getSeatNumber());
        if (existing.isPresent()) {
            throw new ConflictException("Seat already reserved");
        }

        Ticket ticket = ticketMapper.mapRequestToTicket(request, movie, showtime, user);

        Ticket savedTicket = ticketRepository.save(ticket);
        return ticketMapper.toDto(savedTicket);

    }

    //T04 Buy Ticket

    public List<TicketDto> buyTickets(TicketPurchaseRequest request, User user) {

        // 1. Showtime kontrolü
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found"));

        // 2. Geçmiş tarih kontrolü
        LocalDateTime showtimeDateTime = LocalDateTime.of(
                showtime.getDate(),
                showtime.getStartTime()
        );

        if (showtimeDateTime.isBefore(LocalDateTime.now())) {
            throw new ConflictException("Cannot buy tickets for past showtimes");
        }

        // 3. Çakışan koltuk kontrolü
        List<String> seatLetters = request.getSeatInformation().stream()
                .map(s -> s.substring(0, 1)) // "B12" → "B"
                .toList();

        List<Integer> seatNumbers = request.getSeatInformation().stream()
                .map(s -> Integer.parseInt(s.substring(1))) // "B12" → 12
                .toList();

        List<Ticket> existing = ticketRepository
                .findByShowtimeAndSeatLetterInAndSeatNumberIn(showtime, seatLetters, seatNumbers);

        if (!existing.isEmpty()) {
            throw new ConflictException("Some selected seats are already reserved");
        }

        // 4. Yeni ticket’ları oluştur (mapper ile)
        List<Ticket> tickets = request.getSeatInformation().stream()
                .map(seat -> ticketMapper.mapBuyRequestToTicket(seat, showtime, user))
                .toList();

        // 5. Kaydet ve Dto’ya çevir
        List<Ticket> saved = ticketRepository.saveAll(tickets);
        return saved.stream().map(ticketMapper::toDto).toList();
    }
}
