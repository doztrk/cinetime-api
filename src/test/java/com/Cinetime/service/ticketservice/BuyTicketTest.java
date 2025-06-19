package com.Cinetime.service.ticketservice;

import com.Cinetime.entity.*;
import com.Cinetime.enums.PaymentStatus;
import com.Cinetime.enums.TicketStatus;
import com.Cinetime.payload.business.SeatInfo;
import com.Cinetime.payload.dto.request.TicketPurchaseRequest;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.dto.response.TicketResponse;
import com.Cinetime.payload.mappers.TicketMapper;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.repo.*;
import com.Cinetime.service.EmailService;
import com.Cinetime.service.SecurityService;
import com.Cinetime.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;


@ExtendWith(MockitoExtension.class)
class BuyTicketTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private HallRepository hallRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private TicketMapper ticketMapper;

    @Mock
    private EmailService emailService;  // Added missing mock

    @InjectMocks
    private TicketService ticketService;

    private Movie movie;
    private Showtime showtime;
    private Hall hall;
    private Cinema cinema;
    private User user;
    private TicketPurchaseRequest request;
    private List<SeatInfo> seatInfos;

    @BeforeEach
    void setUp() {
        // Setup cinema first
        cinema = new Cinema();
        cinema.setId(1L);
        cinema.setName("Test Cinema");
        cinema.setAddress("123 Test Street");

        // Setup hall with cinema relationship
        hall = new Hall();
        hall.setId(1L);
        hall.setName("Hall A");
        hall.setCinema(cinema);  // This was missing!

        // Setup movie
        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Test Movie");

        // Setup showtime with proper relationships
        showtime = new Showtime();
        showtime.setId(1L);
        showtime.setHall(hall);
        showtime.setMovie(movie);
        showtime.setDate(LocalDate.now());
        showtime.setStartTime(LocalTime.of(19, 0));
        showtime.setEndTime(LocalTime.of(21, 30));

        // Setup user
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstname("Test");
        user.setLastname("User");

        seatInfos = Arrays.asList(
                new SeatInfo("A", 1),
                new SeatInfo("A", 2)
        );

        request = new TicketPurchaseRequest();
        request.setMovieName("Test Movie");
        request.setShowtimeId(1L);
        request.setSeatInfos(seatInfos);
        request.setTicketPrice(100.0);
    }

    @Test
    void buyTickets_WhenMovieNotFound_ShouldReturnNotFoundResponse() {
        // Given
        when(movieRepository.findByTitle(request.getMovieName())).thenReturn(Optional.empty());

        // When
        ResponseMessage<List<TicketResponse>> result = ticketService.buyTickets(request);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, result.getHttpStatus());
        assertEquals(ErrorMessages.MOVIE_NOT_FOUND, result.getMessage());
        assertNull(result.getObject());

        verify(movieRepository).findByTitle(request.getMovieName());
        verifyNoInteractions(showtimeRepository, hallRepository, ticketRepository, paymentRepository);
    }

    @Test
    void buyTickets_WhenShowtimeNotFound_ShouldReturnNotFoundResponse() {
        // Given
        when(movieRepository.findByTitle(request.getMovieName())).thenReturn(Optional.of(movie));
        when(showtimeRepository.findById(request.getShowtimeId())).thenReturn(Optional.empty());

        // When
        ResponseMessage<List<TicketResponse>> result = ticketService.buyTickets(request);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, result.getHttpStatus());
        assertEquals(ErrorMessages.SHOWTIME_NOT_FOUND, result.getMessage());
        assertNull(result.getObject());

        verify(movieRepository).findByTitle(request.getMovieName());
        verify(showtimeRepository).findById(request.getShowtimeId());
        verifyNoInteractions(hallRepository, ticketRepository, paymentRepository);
    }

    @Test
    void buyTickets_WhenNoSeatsSpecified_ShouldReturnBadRequestResponse() {
        // Given
        request.setSeatInfos(null);
        when(movieRepository.findByTitle(request.getMovieName())).thenReturn(Optional.of(movie));
        when(showtimeRepository.findById(request.getShowtimeId())).thenReturn(Optional.of(showtime));
        when(hallRepository.getReferenceById(hall.getId())).thenReturn(hall);

        // When
        ResponseMessage<List<TicketResponse>> result = ticketService.buyTickets(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getHttpStatus());
        assertEquals(ErrorMessages.NO_SEAT_SPECIFIED, result.getMessage());
        assertNull(result.getObject());
    }

    @Test
    void buyTickets_WhenEmptySeatsSpecified_ShouldReturnBadRequestResponse() {
        // Given
        request.setSeatInfos(Collections.emptyList());
        when(movieRepository.findByTitle(request.getMovieName())).thenReturn(Optional.of(movie));
        when(showtimeRepository.findById(request.getShowtimeId())).thenReturn(Optional.of(showtime));
        when(hallRepository.getReferenceById(hall.getId())).thenReturn(hall);

        // When
        ResponseMessage<List<TicketResponse>> result = ticketService.buyTickets(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getHttpStatus());
        assertEquals(ErrorMessages.NO_SEAT_SPECIFIED, result.getMessage());
        assertNull(result.getObject());
    }

    @Test
    void buyTickets_WhenSeatsAlreadyOccupied_ShouldReturnConflictResponse() {
        // Given
        when(movieRepository.findByTitle(request.getMovieName())).thenReturn(Optional.of(movie));
        when(showtimeRepository.findById(request.getShowtimeId())).thenReturn(Optional.of(showtime));
        when(hallRepository.getReferenceById(hall.getId())).thenReturn(hall);

        List<String> occupiedSeats = Arrays.asList("A1", "A2");
        when(ticketRepository.findOccupiedSeatsByShowtimeAndStatus(
                showtime.getId(),
                List.of(TicketStatus.PAID, TicketStatus.RESERVED)
        )).thenReturn(occupiedSeats);

        // When
        ResponseMessage<List<TicketResponse>> result = ticketService.buyTickets(request);

        // Then
        assertEquals(HttpStatus.CONFLICT, result.getHttpStatus());
        assertTrue(result.getMessage().contains(ErrorMessages.SEATS_ARE_OCCUPIED));
        assertTrue(result.getMessage().contains("A1, A2"));
        assertNull(result.getObject());
    }

    @Test
    void buyTickets_WhenEmailSendingFails_ShouldReturnInternalServerErrorResponse() {
        // Given - Setup successful purchase but make email sending fail
        when(movieRepository.findByTitle(request.getMovieName())).thenReturn(Optional.of(movie));
        when(showtimeRepository.findById(request.getShowtimeId())).thenReturn(Optional.of(showtime));
        when(hallRepository.getReferenceById(hall.getId())).thenReturn(hall);
        when(ticketRepository.findOccupiedSeatsByShowtimeAndStatus(
                showtime.getId(),
                List.of(TicketStatus.PAID, TicketStatus.RESERVED)
        )).thenReturn(Collections.emptyList());
        when(securityService.getCurrentUser()).thenReturn(user);

        Payment savedPayment = createMockPayment();
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        List<TicketResponse> ticketResponses = createMockTicketResponses();
        when(ticketMapper.mapTicketToTicketResponse(any(Ticket.class)))
                .thenReturn(ticketResponses.get(0), ticketResponses.get(1));

        // Make email service throw exception
        doThrow(new RuntimeException("Email server is down")).when(emailService).sendMail(any());

        // When
        ResponseMessage<List<TicketResponse>> result = ticketService.buyTickets(request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getHttpStatus());
        assertTrue(result.getMessage().startsWith("Failed to send email:"));
        assertTrue(result.getMessage().contains("Email server is down"));
        assertNull(result.getObject());

        // Verify that payment was still saved (demonstrating the critical bug)
        verify(paymentRepository).save(any(Payment.class));
        verify(emailService).sendMail(any());
    }

    @Test
    void buyTickets_WhenSuccessful_ShouldReturnSuccessResponse() {
        // Given
        when(movieRepository.findByTitle(request.getMovieName())).thenReturn(Optional.of(movie));
        when(showtimeRepository.findById(request.getShowtimeId())).thenReturn(Optional.of(showtime));
        when(hallRepository.getReferenceById(hall.getId())).thenReturn(hall);
        when(ticketRepository.findOccupiedSeatsByShowtimeAndStatus(
                showtime.getId(),
                List.of(TicketStatus.PAID, TicketStatus.RESERVED)
        )).thenReturn(Collections.emptyList());
        when(securityService.getCurrentUser()).thenReturn(user);

        Payment savedPayment = createMockPayment();
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        List<TicketResponse> ticketResponses = createMockTicketResponses();
        when(ticketMapper.mapTicketToTicketResponse(any(Ticket.class)))
                .thenReturn(ticketResponses.get(0), ticketResponses.get(1));

        // Mock email service to not throw exception
        doNothing().when(emailService).sendMail(any());

        // When
        ResponseMessage<List<TicketResponse>> result;
        try {
            result = ticketService.buyTickets(request);
        } catch (Exception e) {
            // Print the actual exception to debug
            e.printStackTrace();
            fail("Service method threw unexpected exception: " + e.getMessage());
            return;
        }

        // Then - Check if we got an error response instead of success
        if (result.getHttpStatus() != HttpStatus.OK) {
            fail("Expected OK but got " + result.getHttpStatus() +
                    " with message: " + result.getMessage());
        }

        assertEquals(HttpStatus.OK, result.getHttpStatus());
        assertEquals(SuccessMessages.TICKET_BOUGHT_SUCCESSFULLY, result.getMessage());
        assertEquals(2, result.getObject().size());

        // Verify payment was created correctly
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());

        Payment capturedPayment = paymentCaptor.getValue();
        assertEquals(user, capturedPayment.getUser());
        assertEquals(request.getTicketPrice(), capturedPayment.getAmount());
        assertEquals(PaymentStatus.SUCCESS, capturedPayment.getPaymentStatus());
        assertEquals(2, capturedPayment.getTickets().size());

        // Verify tickets were created correctly
        Set<Ticket> tickets = capturedPayment.getTickets();
        assertEquals(2, tickets.size());

        double expectedPricePerTicket = request.getTicketPrice() / seatInfos.size();
        for (Ticket ticket : tickets) {
            assertEquals(movie, ticket.getMovie());
            assertEquals(showtime, ticket.getShowtime());
            assertEquals(user, ticket.getUser());
            assertEquals(hall, ticket.getHall());
            assertEquals(expectedPricePerTicket, ticket.getPrice());
            assertEquals(TicketStatus.PAID, ticket.getStatus());
            assertEquals(capturedPayment, ticket.getPayment());
        }

        // Verify email was sent
        verify(emailService).sendMail(any());
    }

    @Test
    void buyTickets_WhenPartialSeatsOccupied_ShouldReturnConflictResponse() {
        // Given
        when(movieRepository.findByTitle(request.getMovieName())).thenReturn(Optional.of(movie));
        when(showtimeRepository.findById(request.getShowtimeId())).thenReturn(Optional.of(showtime));
        when(hallRepository.getReferenceById(hall.getId())).thenReturn(hall);

        // Only one seat is occupied
        List<String> occupiedSeats = Arrays.asList("A1");
        when(ticketRepository.findOccupiedSeatsByShowtimeAndStatus(
                showtime.getId(),
                List.of(TicketStatus.PAID, TicketStatus.RESERVED)
        )).thenReturn(occupiedSeats);

        // When
        ResponseMessage<List<TicketResponse>> result = ticketService.buyTickets(request);

        // Then
        assertEquals(HttpStatus.CONFLICT, result.getHttpStatus());
        assertTrue(result.getMessage().contains(ErrorMessages.SEATS_ARE_OCCUPIED));
        assertTrue(result.getMessage().contains("A1"));
        assertFalse(result.getMessage().contains("A2"));
        assertNull(result.getObject());
    }

    private Payment createMockPayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setUser(user);
        payment.setAmount(request.getTicketPrice());
        payment.setPaymentStatus(PaymentStatus.SUCCESS);

        Set<Ticket> tickets = new HashSet<>();
        for (int i = 0; i < seatInfos.size(); i++) {
            SeatInfo seatInfo = seatInfos.get(i);
            Ticket ticket = new Ticket();
            ticket.setId((long) (i + 1));
            ticket.setMovie(movie);
            ticket.setShowtime(showtime);
            ticket.setUser(user);
            ticket.setHall(hall);
            ticket.setSeatLetter(seatInfo.getSeatLetter());
            ticket.setSeatNumber(seatInfo.getSeatNumber());
            ticket.setPrice(request.getTicketPrice() / seatInfos.size());
            ticket.setStatus(TicketStatus.PAID);
            ticket.setPayment(payment);
            tickets.add(ticket);
        }
        payment.setTickets(tickets);

        return payment;
    }

    private List<TicketResponse> createMockTicketResponses() {
        return Arrays.asList(
                TicketResponse.builder()
                        .id(1L)
                        .seatLetter("A")
                        .seatNumber(1)
                        .price(50.0)
                        .movieName("Test Movie")
                        .showTimeId(1L)
                        .showTimeDate(LocalDate.now())
                        .startTime(LocalTime.of(19, 0))
                        .endTime(LocalTime.of(21, 30))
                        .ticketOwnerNameSurname("Test User")
                        .hallName("Hall A")
                        .cinemaName("Test Cinema")
                        .cinemaAdress("123 Test Street")
                        .createdAt(LocalDateTime.now())
                        .status(TicketStatus.PAID)
                        .build(),
                TicketResponse.builder()
                        .id(2L)
                        .seatLetter("A")
                        .seatNumber(2)
                        .price(50.0)
                        .movieName("Test Movie")
                        .showTimeId(1L)
                        .showTimeDate(LocalDate.now())
                        .startTime(LocalTime.of(19, 0))
                        .endTime(LocalTime.of(21, 30))
                        .ticketOwnerNameSurname("Test User")
                        .hallName("Hall A")
                        .cinemaName("Test Cinema")
                        .cinemaAdress("123 Test Street")
                        .createdAt(LocalDateTime.now())
                        .status(TicketStatus.PAID)
                        .build()
        );
    }
}