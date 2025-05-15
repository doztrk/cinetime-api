package com.Cinetime.service;

import com.Cinetime.entity.*;
import com.Cinetime.enums.PaymentStatus;
import com.Cinetime.enums.TicketStatus;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.payload.business.SeatInfo;
import com.Cinetime.payload.dto.request.*;
import com.Cinetime.payload.dto.response.AnonymousTicketResponse;
import com.Cinetime.payload.dto.response.TicketResponse;
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
    private final AnonymousUserRepository anonymousUserRepository;
    private final EmailService emailService;

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
    public ResponseMessage<List<TicketResponse>> reserveTicket(TicketReserveRequest request) {


        Optional<Movie> movieOptional = movieRepository.findByTitle(request.getMovieName());

        if (movieOptional.isEmpty()) {
            return ResponseMessage.<List<TicketResponse>>builder()
                    .message(ErrorMessages.MOVIE_NOT_FOUND)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        Movie movie = movieOptional.get();

        Optional<Showtime> showTimeOptional = showtimeRepository.findById(request.getShowtimeId());

        if (showTimeOptional.isEmpty()) {
            return ResponseMessage.<List<TicketResponse>>builder()
                    .message(ErrorMessages.SHOWTIME_NOT_FOUND)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        Showtime showtime = showTimeOptional.get();

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


        Payment payment = new Payment();

        payment.setUser(user);
        payment.setAmount(request.getTicketPrice());

        payment.setPaymentStatus(PaymentStatus.PENDING);

        Set<Ticket> ticketSet = new HashSet<>();

        payment.setTickets(ticketSet);

        double pricePerTicket = request.getTicketPrice() / requestedSeats.size();

        for (SeatInfo seatInfo : requestedSeats) {
            Ticket ticket = Ticket.builder()
                    .movie(movie)
                    .showtime(showtime)
                    .user(user)
                    .hall(hall)
                    .seatLetter(seatInfo.getSeatLetter())
                    .seatNumber(seatInfo.getSeatNumber())
                    .price(pricePerTicket)
                    .status(TicketStatus.RESERVED)
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
                .message(SuccessMessages.TICKET_RESERVED_SUCCESSFULLY)
                .httpStatus(HttpStatus.OK)
                .object(ticketResponses)
                .build();

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

        Optional<Showtime> showTimeOptional = showtimeRepository.findById(request.getShowtimeId());

        if (showTimeOptional.isEmpty()) {
            return ResponseMessage.<List<TicketResponse>>builder()
                    .message(ErrorMessages.SHOWTIME_NOT_FOUND)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        Showtime showtime = showTimeOptional.get();

        Hall hall = hallRepository.getReferenceById(showtime.getHall().getId());
        /*Hall hall = showtime.getHall();*/

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


        Payment payment = new Payment();

        payment.setUser(user);
        payment.setAmount(request.getTicketPrice());

        payment.setPaymentStatus(PaymentStatus.SUCCESS);

        Set<Ticket> ticketSet = new HashSet<>();

        payment.setTickets(ticketSet);

        double pricePerTicket = request.getTicketPrice() / requestedSeats.size();

        for (SeatInfo seatInfo : requestedSeats) {
            Ticket ticket = Ticket.builder()
                    .movie(movie)
                    .showtime(showtime)
                    .user(user)
                    .hall(hall)
                    .seatLetter(seatInfo.getSeatLetter())
                    .seatNumber(seatInfo.getSeatNumber())
                    .price(pricePerTicket)
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

    public ResponseMessage<Double> calculateTicketPrice(TicketPriceCalculationRequest request) {


        boolean showtimeExists = showtimeRepository.existsById(request.getShowtimeId());
        if (!showtimeExists) {
            return ResponseMessage.<Double>builder()
                    .message(ErrorMessages.SHOWTIME_NOT_FOUND)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }
        Double price = showtimeRepository.findShowtimePriceByshowtimeId(request.getShowtimeId());

        Double totalPrice = price * request.getSeats().size();

        return ResponseMessage.<Double>builder()
                .message(SuccessMessages.TICKET_PRICE_CALCULATED_SUCCESSFULLY)
                .httpStatus(HttpStatus.OK)
                .object(totalPrice)
                .build();
    }

    @Transactional
    public ResponseMessage<List<AnonymousTicketResponse>> buyTicketsAsGuest(TicketPurchaseGuestRequest request) {

        Optional<Movie> movieOptional = movieRepository.findByTitle(request.getMovieName());

        if (movieOptional.isEmpty()) {
            return ResponseMessage.<List<AnonymousTicketResponse>>builder()
                    .message(ErrorMessages.MOVIE_NOT_FOUND)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        Movie movie = movieOptional.get();

        Optional<Showtime> showTimeOptional = showtimeRepository.findById(request.getShowtimeId());

        if (showTimeOptional.isEmpty()) {
            return ResponseMessage.<List<AnonymousTicketResponse>>builder()
                    .message(ErrorMessages.SHOWTIME_NOT_FOUND)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        Showtime showtime = showTimeOptional.get();

        Optional<Hall> hallOptional = hallRepository.findById(showtime.getHall().getId());
        if (hallOptional.isEmpty()) {
            return ResponseMessage.<List<AnonymousTicketResponse>>builder()
                    .message(ErrorMessages.HALL_NOT_FOUND)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        Hall hall = hallOptional.get();

        List<SeatInfo> requestedSeats = request.getSeatInfos();

        List<String> takenSeats = ticketRepository
                .findOccupiedSeatsByShowtimeAndStatus(showtime.getId(), List.of(TicketStatus.PAID, TicketStatus.RESERVED));

        List<String> alreadyReservedSeats = requestedSeats
                .stream()
                .map(SeatInfo::getFullSeatName)
                .filter(takenSeats::contains).toList();

        if (!alreadyReservedSeats.isEmpty()) {
            return ResponseMessage.<List<AnonymousTicketResponse>>builder()
                    .message(ErrorMessages.SEATS_ARE_OCCUPIED)
                    .httpStatus(HttpStatus.CONFLICT)
                    .build();
        }

        AnonymousUser anonymousUser = new AnonymousUser();

        anonymousUser.setEmail(request.getAnonymousUser().getEmail());
        anonymousUser.setFullName(request.getAnonymousUser().getFullName());
        anonymousUser.setPhoneNumber(request.getAnonymousUser().getPhoneNumber());
        anonymousUser.setRetrievalCode(UUID.randomUUID().toString());

        AnonymousUser savedAnonymousUser = anonymousUserRepository.save(anonymousUser);

        String retrievalCode = savedAnonymousUser.getRetrievalCode();

        Payment payment = new Payment();
        payment.setAnonymousUser(savedAnonymousUser);
        payment.setAmount(request.getTicketPrice());
        payment.setPaymentStatus(PaymentStatus.SUCCESS);


        Set<Ticket> ticketSet = new HashSet<>();
        payment.setTickets(ticketSet);

        double pricePerTicket = request.getTicketPrice() / requestedSeats.size();

        for (SeatInfo seatInfo : requestedSeats) {
            Ticket ticket = Ticket.builder()
                    .movie(movie)
                    .showtime(showtime)
                    .anonymousUser(savedAnonymousUser)
                    .hall(hall)
                    .seatLetter(seatInfo.getSeatLetter())
                    .seatNumber(seatInfo.getSeatNumber())
                    .price(pricePerTicket)
                    .status(TicketStatus.PAID)
                    .payment(payment)
                    .build();
            ticketSet.add(ticket);
        }

        Payment savedPayment = paymentRepository.save(payment);

        List<AnonymousTicketResponse> ticketResponses = savedPayment
                .getTickets()
                .stream()
                .map(ticket -> ticketMapper.mapTicketToAnonymousTicketResponse(ticket, retrievalCode))
                .toList();

        try {
            sendTicketConfirmationEmail(savedAnonymousUser, movie, showtime, ticketResponses, request.getTicketPrice());
        } catch (Exception e) {
            return ResponseMessage.<List<AnonymousTicketResponse>>builder()
                    .message("Failed to send email: " + e.getMessage())
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
        return ResponseMessage.<List<AnonymousTicketResponse>>builder()
                .message(SuccessMessages.TICKET_BOUGHT_SUCCESSFULLY)
                .httpStatus(HttpStatus.OK)
                .object(ticketResponses)
                .build();

    }

    private void sendTicketConfirmationEmail(User user, Movie movie, Showtime showtime,
                                             List<TicketResponse> tickets, Double totalPrice) {
        MailRequest mailRequest = MailRequest.builder()
                .to(user.getEmail())
                .subject("Film adı - " + movie.getTitle())
                .movieName(movie.getTitle())
                .total(String.format("%.2f TL", totalPrice))
                .cinemaName(showtime.getHall().getCinema().getName())
                .cinemaAddress(showtime.getHall().getCinema().getAddress())
                .hallName(showtime.getHall().getName())
                .seatInfos(tickets.stream()
                        .map(ticket -> new SeatInfo(ticket.getSeatLetter(), ticket.getSeatNumber()))
                        .toList())
                .date(showtime.getDate())
                .startTime(showtime.getStartTime().toString())
                .endTime(showtime.getEndTime().toString())
                .adress(showtime.getHall().getCinema().getAddress())
                .build();

        emailService.sendMail(mailRequest);
    }

    private void sendTicketConfirmationEmail(AnonymousUser user, Movie movie, Showtime showtime,
                                             List<AnonymousTicketResponse> tickets, Double totalPrice) {
        MailRequest mailRequest = MailRequest.builder()
                .to(user.getEmail())
                .subject("Film adı - " + movie.getTitle())
                .movieName(movie.getTitle())
                .total(String.format("%.2f TL", totalPrice))
                .cinemaName(showtime.getHall().getCinema().getName())
                .cinemaAddress(showtime.getHall().getCinema().getAddress())
                .hallName(showtime.getHall().getName())
                .seatInfos(tickets.stream()
                        .map(ticket -> new SeatInfo(ticket.getTicketResponse().getSeatLetter(), ticket.getTicketResponse().getSeatNumber()))
                        .toList())
                .date(showtime.getDate())
                .startTime(showtime.getStartTime().toString())
                .endTime(showtime.getEndTime().toString())
                .adress(showtime.getHall().getCinema().getAddress())
                .retrievalCode(user.getRetrievalCode())
                .build();

        emailService.sendMail(mailRequest);
    }
}

