package com.Cinetime.service;

import com.Cinetime.entity.*;
import com.Cinetime.enums.PaymentStatus;
import com.Cinetime.enums.TicketStatus;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.payload.business.SeatInfo;
import com.Cinetime.payload.dto.request.TicketPurchaseRequest;
import com.Cinetime.payload.dto.response.TicketPriceResponse;
import com.Cinetime.payload.dto.response.TicketResponse;
import com.Cinetime.payload.dto.request.TicketReserveRequest;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.mappers.TicketMapper;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.repo.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final PageableHelper pageableHelper;
    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SecurityService securityService;
    private final HallRepository hallRepository;
    private final PaymentRepository paymentRepository;

    //T01 Return movies that an authenticated user bought and haven't used yet
    public ResponseMessage<Page<TicketResponse>> getCurrentTickets(int page, int size, String sort, String type) {

        User user = securityService.getCurrentUser();


        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);
        Page<Ticket> tickets = ticketRepository.findByUserAndStatus(user, TicketStatus.PAID, pageable);
        Page<TicketResponse> ticketResponses = tickets.map(ticketMapper::mapTicketToTicketResponse);

        return ResponseMessage.<Page<TicketResponse>>builder()
                .message(SuccessMessages.TICKETS_FOUND)
                .object(ticketResponses)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    //T02 Return movies that an authenticated user bought and used
    public ResponseMessage<Page<TicketResponse>> getPassedTickets(int page, int size, String sort, String type) {

        User user = securityService.getCurrentUser();

        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);
        Page<Ticket> tickets = ticketRepository.findByUserAndStatus(user, TicketStatus.USED, pageable);

        Page<TicketResponse> TicketResponses = tickets.map(ticketMapper::mapTicketToTicketResponse);

        return ResponseMessage.<Page<TicketResponse>>builder()
                .message(SuccessMessages.TICKETS_FOUND)
                .object(TicketResponses)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    //T03 reserve movie ticket
    @Transactional
    public ResponseMessage<List<TicketResponse>> reserveTicket(TicketReserveRequest request, Long movieId) {


        Optional<Movie> movieOptional = movieRepository.findById(movieId);


        //Movie Check
        if (movieOptional.isEmpty()) {
            return ResponseMessage.<List<TicketResponse>>builder()
                    .message(ErrorMessages.MOVIE_NOT_FOUND)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }
        Movie movie = movieOptional.get();

        Optional<Showtime> showtimeOptional = showtimeRepository.findById(request.getShowtimeId());


        //Showtime Check
        if (showtimeOptional.isEmpty()) {
            return ResponseMessage.<List<TicketResponse>>builder()
                    .message(ErrorMessages.SHOWTIME_NOT_FOUND)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }
        Showtime showtime = showtimeOptional.get();
        Hall hall = showtime.getHall();

        // Koltuk başka biri tarafından rezerve edilmiş mi (REZERVE VEYA PAID olanlar kontrol edilir)

        List<SeatInfo> requestedSeats = request.getSeatInfos();
        if (requestedSeats == null || requestedSeats.isEmpty()) {
            return ResponseMessage.<List<TicketResponse>>builder()
                    .message(ErrorMessages.NO_SEAT_SPECIFIED)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }
        List<Ticket> existingTickets = ticketRepository.findByShowtimeAndStatusIn(showtime, List.of(TicketStatus.PAID, TicketStatus.RESERVED));

        Set<String> takenSeats = existingTickets
                .stream()
                .map(ticket -> ticket.getSeatLetter() + ticket.getSeatNumber())
                .collect(Collectors.toSet());

        List<String> alreadyReservedSeats = requestedSeats
                .stream()
                .map(SeatInfo::getFullSeatName)
                .filter(takenSeats::contains).toList();

        if (!alreadyReservedSeats.isEmpty()) {
            return ResponseMessage.<List<TicketResponse>>builder()
                    .message(ErrorMessages.SEATS_ARE_OCCUPIED + String.join(",", alreadyReservedSeats))
                    .httpStatus(HttpStatus.CONFLICT)
                    .build();
        }

        try {
            User user = securityService.getCurrentUser();


            double totalAmount = requestedSeats
                    .stream()
                    .mapToDouble(SeatInfo::getPrice).sum();


            Payment payment = new Payment();

            payment.setUser(user);
            payment.setAmount(totalAmount);
            payment.setPaymentStatus(PaymentStatus.PENDING);


            Set<Ticket> ticketSet = new HashSet<>();
            payment.setTickets(ticketSet);


            for (SeatInfo seatInfo : requestedSeats) {
                Ticket ticket = new Ticket();

                ticket.setMovie(movie);
                ticket.setShowtime(showtime);
                ticket.setHall(hall);
                ticket.setUser(user);
                ticket.setSeatLetter(seatInfo.getSeatLetter());
                ticket.setSeatNumber(seatInfo.getSeatNumber());
                ticket.setPrice(seatInfo.getPrice());
                ticket.setStatus(TicketStatus.RESERVED);
                ticket.setPayment(payment);

                ticketSet.add(ticket);
            }


            Payment savedPayment = paymentRepository.save(payment);

            List<TicketResponse> responses = savedPayment.getTickets().stream()
                    .map(ticketMapper::mapTicketToTicketResponse)
                    .collect(Collectors.toList());


            return ResponseMessage.<List<TicketResponse>>builder()
                    .message(SuccessMessages.TICKET_RESERVED)
                    .object(responses)
                    .httpStatus(HttpStatus.OK)
                    .build();

        } catch (Exception e) {
            return ResponseMessage.<List<TicketResponse>>builder()
                    .message(ErrorMessages.PAYMENT_ERROR)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }

    }


    @Transactional
    public ResponseMessage<List<TicketResponse>> buyTickets(TicketPurchaseRequest request) {

        Optional<Movie> movieOptional = movieRepository.findByTitle(request.getMovieName());

        if (movieOptional.isEmpty()) {
            return ResponseMessage.<List<TicketResponse>>builder()
                    .message(ErrorMessages.MOVIE_NOT_FOUND)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        Movie movie = movieOptional.get();

        Optional<Showtime> showTimeOptional = showtimeRepository.findById(request.getShowTimeId());

        if (showTimeOptional.isEmpty()) {
            return ResponseMessage.<List<TicketResponse>>builder()
                    .message(ErrorMessages.SHOWTIME_NOT_FOUND)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        Showtime showtime = showTimeOptional.get();


        /*Hall hall = showtime.getHall();*/

        Hall hall = hallRepository.getReferenceById(showtime.getHall().getId());


        List<SeatInfo> requestedSeats = request.getSeatInfos();

        if (requestedSeats == null || requestedSeats.isEmpty()) {
            return ResponseMessage.<List<TicketResponse>>builder()
                    .message(ErrorMessages.NO_SEAT_SPECIFIED)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }
        List<String> takenSeats = ticketRepository
                .findOccupiedSeatsByShowtimeAndStatus(showtime.getId(), List.of(TicketStatus.PAID, TicketStatus.RESERVED));


        List<String> alreadyReservedSeats = requestedSeats
                .stream()
                .map(SeatInfo::getFullSeatName)
                .filter(takenSeats::contains).toList();

        if (!alreadyReservedSeats.isEmpty()) {
            return ResponseMessage.<List<TicketResponse>>builder()
                    .message(ErrorMessages.SEATS_ARE_OCCUPIED + String.join(", ", alreadyReservedSeats))
                    .httpStatus(HttpStatus.CONFLICT)
                    .build();
        }


        User user = securityService.getCurrentUser();

        double totalAmount = requestedSeats.stream().mapToDouble(SeatInfo::getPrice).sum();

        Payment payment = new Payment();

        payment.setUser(user);
        payment.setAmount(totalAmount);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);

        Set<Ticket> ticketSet = new HashSet<>();

        payment.setTickets(ticketSet);

        for (SeatInfo seatInfo : requestedSeats) {
            Ticket ticket = Ticket.builder()
                    .movie(movie)
                    .showtime(showtime)
                    .user(user)
                    .hall(hall)
                    .seatLetter(seatInfo.getSeatLetter())
                    .seatNumber(seatInfo.getSeatNumber())
                    .price(seatInfo.getPrice())
                    .status(TicketStatus.PAID)
                    .payment(payment)
                    .build();

            ticketSet.add(ticket);
        }

        Payment savedPayment = paymentRepository.save(payment);

        List<TicketResponse> ticketResponses = savedPayment
                .getTickets()
                .stream()
                .map(ticketMapper::mapTicketToTicketResponse)
                .toList();

        return ResponseMessage.<List<TicketResponse>>builder()
                .message(SuccessMessages.TICKET_BOUGHT_SUCCESSFULLY)
                .httpStatus(HttpStatus.OK)
                .object(ticketResponses)
                .build();


    }


    public ResponseMessage<Double> getTicketPrice(Long showtimeId) {
        Double showtimePrice = showtimeRepository.findShowtimePriceByshowtimeId(showtimeId);

        return ResponseMessage.<Double>builder()
                .message(SuccessMessages.TICKET_PRICE_FOUND_SUCCESSFULLY)
                .httpStatus(HttpStatus.OK)
                .object(showtimePrice)
                .build();
    }
}

