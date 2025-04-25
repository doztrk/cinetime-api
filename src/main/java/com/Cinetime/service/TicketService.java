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
import com.Cinetime.payload.dto.request.SeatRequest;
import com.Cinetime.payload.dto.request.TicketPurchaseRequest;
import com.Cinetime.payload.dto.request.TicketRequestDto;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.mappers.TicketMapper;
import com.Cinetime.repo.MovieRepository;
import com.Cinetime.repo.ShowtimeRepository;
import com.Cinetime.repo.TicketRepository;
import com.Cinetime.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public ResponseMessage<Page<TicketDto>> getCurrentTickets(Authentication authentication, int page, int size, String sort, String type) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();

        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);
        Page<Ticket> tickets = ticketRepository.findByUserAndStatus(user, TicketStatus.RESERVED, pageable);
        Page<TicketDto> ticketDtos = tickets.map(ticketMapper::toDto);

        return ResponseMessage.<Page<TicketDto>>builder()
                .message("Current tickets retrieved successfully")
                .object(ticketDtos)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    //T02 Return movies that an authenticated user bought and used
    public ResponseMessage<Page<TicketDto>> getPassedTickets(Authentication authentication, int page, int size, String sort, String type) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();

        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);
        Page<Ticket> tickets = ticketRepository.findByUserAndStatus(user, TicketStatus.USED, pageable);
        Page<TicketDto> ticketDtos = tickets.map(ticketMapper::toDto);

        return ResponseMessage.<Page<TicketDto>>builder()
                .message("Passed tickets retrieved successfully")
                .object(ticketDtos)
                .httpStatus(HttpStatus.OK)
                .build();
    }
    //T03 reserve movie ticket
    public ResponseMessage<TicketDto> reserveTicket(TicketRequestDto request, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));

        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found"));

        // Koltuk başka biri tarafından rezerve edilmiş mi (REZERVE VEYA PAID olanlar kontrol edilir)
        Optional<Ticket> existing = ticketRepository
                .findByShowtimeAndSeatLetterAndSeatNumberAndStatusIn(
                        showtime,
                        request.getSeatLetter(),
                        request.getSeatNumber(),
                        List.of(TicketStatus.RESERVED, TicketStatus.PAID)
                );

        if (existing.isPresent()) {
            throw new ConflictException("Seat already reserved");
        }

        Ticket ticket = ticketMapper.mapRequestToTicket(request, movie, showtime, user);
        ticket.setStatus(TicketStatus.RESERVED);  // TicketStatus ayarlanıyor

        Ticket savedTicket = ticketRepository.save(ticket);
        TicketDto ticketDto = ticketMapper.toDto(savedTicket);

        return ResponseMessage.<TicketDto>builder()
                .message("Ticket reserved successfully")
                .object(ticketDto)
                .httpStatus(HttpStatus.CREATED)
                .build();
    }

    //T04 Buy Ticket

    public ResponseMessage<List<TicketDto>> buyTickets(TicketPurchaseRequest request, Authentication authentication) {
        // Giriş yapmamış (anonim) kullanıcı kontrolü
        User user = (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String))
                ? ((UserDetailsImpl) authentication.getPrincipal()).getUser()
                : null;

        // Movie ve Showtime kontrolü
        Movie movie = movieRepository.findByTitle(request.getMovieName())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with name: " + request.getMovieName()));

        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with ID: " + request.getShowtimeId()));

        List<TicketDto> purchasedTickets = new ArrayList<>();

        // Her koltuk için rezervasyon kontrolü ve kaydetme
        for (SeatRequest seat : request.getSeats()) {
            String seatLetter = seat.getSeatLetter();
            Integer seatNumber = seat.getSeatNumber();

            // Aynı koltuk daha önce rezerve edilmiş veya satın alınmış mı?
            Optional<Ticket> existingTicket = ticketRepository
                    .findByShowtimeAndSeatLetterAndSeatNumberAndStatusIn(
                            showtime,
                            seatLetter,
                            seatNumber,
                            List.of(TicketStatus.RESERVED, TicketStatus.PAID)
                    );

            if (existingTicket.isPresent()) {
                throw new ConflictException("Seat " + seatLetter + seatNumber + " is already reserved or sold.");
            }

            // Ticket oluşturuluyor
            Ticket ticket = new Ticket();
            ticket.setMovie(movie);
            ticket.setShowtime(showtime);
            ticket.setSeatLetter(seatLetter);
            ticket.setSeatNumber(seatNumber);
            ticket.setUser(user); // Anonimse null kalabilir
            ticket.setStatus(TicketStatus.PAID); // Satın alma işlemi olduğu için PAID

            Ticket savedTicket = ticketRepository.save(ticket);
            TicketDto ticketDto = ticketMapper.toDto(savedTicket);
            purchasedTickets.add(ticketDto);
        }

        return ResponseMessage.<List<TicketDto>>builder()
                .message("Tickets purchased successfully")
                .object(purchasedTickets)
                .httpStatus(HttpStatus.CREATED)
                .build();
    }

}

