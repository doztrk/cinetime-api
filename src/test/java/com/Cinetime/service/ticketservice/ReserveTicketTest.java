package com.Cinetime.service.ticketservice;

import com.Cinetime.entity.*;
import com.Cinetime.enums.PaymentStatus;
import com.Cinetime.enums.TicketStatus;
import com.Cinetime.payload.business.SeatInfo;
import com.Cinetime.payload.dto.request.TicketReserveRequest;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.dto.response.TicketResponse;
import com.Cinetime.payload.mappers.TicketMapper;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.repo.*;
import com.Cinetime.service.SecurityService;
import com.Cinetime.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReserveTicketTest {

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

    @InjectMocks
    private TicketService ticketReservationService;

    private Movie movie;
    private Showtime showtime;
    private Hall hall;
    private User user;
    private TicketReserveRequest request;
    private List<SeatInfo> seatInfos;
    private Payment payment;

    @BeforeEach
    void setUp() {
        // Setup test data
        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Test Movie");

        hall = new Hall();
        hall.setId(1L);

        showtime = new Showtime();
        showtime.setId(1L);
        showtime.setHall(hall);

        user = new User();
        user.setId(1L);
        user.setFirstname("testuser");

        seatInfos = Arrays.asList(
                new SeatInfo("A", 1),
                new SeatInfo("A", 2)
        );

        request = new TicketReserveRequest();
        request.setMovieName("Test Movie");
        request.setShowtimeId(1L);
        request.setSeatInfos(seatInfos);
        request.setTicketPrice(20.0);

        payment = new Payment();
        payment.setId(1L);
        payment.setTickets(new HashSet<>());
    }

    @Test
    void reserveTicket_Success() {
        // Given
        when(movieRepository.findByTitle("Test Movie")).thenReturn(Optional.of(movie));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(hallRepository.getReferenceById(1L)).thenReturn(hall);
        when(ticketRepository.findOccupiedSeatsByShowtimeAndStatus(1L,
                List.of(TicketStatus.PAID, TicketStatus.RESERVED))).thenReturn(List.of());
        when(securityService.getCurrentUser()).thenReturn(user);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment inputPayment = invocation.getArgument(0);
            // Return the payment with its tickets populated
            Payment savedPayment = new Payment();
            savedPayment.setId(1L);
            savedPayment.setUser(inputPayment.getUser());
            savedPayment.setAmount(inputPayment.getAmount());
            savedPayment.setPaymentStatus(inputPayment.getPaymentStatus());
            savedPayment.setTickets(inputPayment.getTickets());
            return savedPayment;
        });
        when(ticketMapper.mapTicketToTicketResponse(any(Ticket.class)))
                .thenAnswer(invocation -> {
                    Ticket ticket = invocation.getArgument(0);
                    TicketResponse response = new TicketResponse();
                    response.setSeatLetter(ticket.getSeatLetter());
                    response.setSeatNumber(ticket.getSeatNumber());
                    response.setPrice(ticket.getPrice());
                    return response;
                });

        // When
        ResponseMessage<List<TicketResponse>> result = ticketReservationService.reserveTicket(request);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.TICKET_RESERVED_SUCCESSFULLY);
        assertThat(result.getObject()).hasSize(2);

        verify(paymentRepository).save(argThat(savedPayment -> {
            assertThat(savedPayment.getUser()).isEqualTo(user);
            assertThat(savedPayment.getAmount()).isEqualTo(20.0);
            assertThat(savedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(savedPayment.getTickets()).hasSize(2);

            // Verify ticket details
            savedPayment.getTickets().forEach(ticket -> {
                assertThat(ticket.getMovie()).isEqualTo(movie);
                assertThat(ticket.getShowtime()).isEqualTo(showtime);
                assertThat(ticket.getUser()).isEqualTo(user);
                assertThat(ticket.getHall()).isEqualTo(hall);
                assertThat(ticket.getPrice()).isEqualTo(10.0); // 20.0 / 2 seats
                assertThat(ticket.getStatus()).isEqualTo(TicketStatus.RESERVED);
                assertThat(ticket.getPayment()).isEqualTo(savedPayment);
            });

            return true;
        }));
    }

    @Test
    void reserveTicket_MovieNotFound() {
        // Given
        when(movieRepository.findByTitle("Test Movie")).thenReturn(Optional.empty());

        // When
        ResponseMessage<List<TicketResponse>> result = ticketReservationService.reserveTicket(request);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.MOVIE_NOT_FOUND);
        assertThat(result.getObject()).isNull();

        verifyNoInteractions(showtimeRepository, hallRepository, ticketRepository,
                paymentRepository, securityService);
    }

    @Test
    void reserveTicket_ShowtimeNotFound() {
        // Given
        when(movieRepository.findByTitle("Test Movie")).thenReturn(Optional.of(movie));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        ResponseMessage<List<TicketResponse>> result = ticketReservationService.reserveTicket(request);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.SHOWTIME_NOT_FOUND);
        assertThat(result.getObject()).isNull();

        verifyNoInteractions(hallRepository, ticketRepository, paymentRepository, securityService);
    }

    @Test
    void reserveTicket_NoSeatsSpecified_NullSeats() {
        // Given
        request.setSeatInfos(null);
        when(movieRepository.findByTitle("Test Movie")).thenReturn(Optional.of(movie));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(hallRepository.getReferenceById(1L)).thenReturn(hall);

        // When
        ResponseMessage<List<TicketResponse>> result = ticketReservationService.reserveTicket(request);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.NO_SEAT_SPECIFIED);
        assertThat(result.getObject()).isNull();

        verifyNoInteractions(ticketRepository, paymentRepository, securityService);
    }

    @Test
    void reserveTicket_NoSeatsSpecified_EmptySeats() {
        // Given
        request.setSeatInfos(List.of());
        when(movieRepository.findByTitle("Test Movie")).thenReturn(Optional.of(movie));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(hallRepository.getReferenceById(1L)).thenReturn(hall);

        // When
        ResponseMessage<List<TicketResponse>> result = ticketReservationService.reserveTicket(request);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.NO_SEAT_SPECIFIED);
        assertThat(result.getObject()).isNull();
    }

    @Test
    void reserveTicket_SeatsAlreadyOccupied() {
        // Given
        when(movieRepository.findByTitle("Test Movie")).thenReturn(Optional.of(movie));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(hallRepository.getReferenceById(1L)).thenReturn(hall);
        when(ticketRepository.findOccupiedSeatsByShowtimeAndStatus(1L,
                List.of(TicketStatus.PAID, TicketStatus.RESERVED)))
                .thenReturn(List.of("A1", "A2"));

        // When
        ResponseMessage<List<TicketResponse>> result = ticketReservationService.reserveTicket(request);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(result.getMessage()).contains(ErrorMessages.SEATS_ARE_OCCUPIED);
        assertThat(result.getMessage()).contains("A1, A2");
        assertThat(result.getObject()).isNull();

        verifyNoInteractions(paymentRepository, securityService);
    }

    @Test
    void reserveTicket_PartialSeatsOccupied() {
        // Given
        when(movieRepository.findByTitle("Test Movie")).thenReturn(Optional.of(movie));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(hallRepository.getReferenceById(1L)).thenReturn(hall);
        when(ticketRepository.findOccupiedSeatsByShowtimeAndStatus(1L,
                List.of(TicketStatus.PAID, TicketStatus.RESERVED)))
                .thenReturn(List.of("A1")); // Only A1 is occupied

        // When
        ResponseMessage<List<TicketResponse>> result = ticketReservationService.reserveTicket(request);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(result.getMessage()).contains(ErrorMessages.SEATS_ARE_OCCUPIED);
        assertThat(result.getMessage()).contains("A1");
        assertThat(result.getMessage()).doesNotContain("A2");
        assertThat(result.getObject()).isNull();
    }

    @Test
    void reserveTicket_SingleSeat() {
        // Given
        List<SeatInfo> singleSeat = List.of(new SeatInfo("B", 5));
        request.setSeatInfos(singleSeat);
        request.setTicketPrice(15.0);

        when(movieRepository.findByTitle("Test Movie")).thenReturn(Optional.of(movie));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(hallRepository.getReferenceById(1L)).thenReturn(hall);
        when(ticketRepository.findOccupiedSeatsByShowtimeAndStatus(1L,
                List.of(TicketStatus.PAID, TicketStatus.RESERVED))).thenReturn(List.of());
        when(securityService.getCurrentUser()).thenReturn(user);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment inputPayment = invocation.getArgument(0);
            Payment savedPayment = new Payment();
            savedPayment.setId(1L);
            savedPayment.setUser(inputPayment.getUser());
            savedPayment.setAmount(inputPayment.getAmount());
            savedPayment.setPaymentStatus(inputPayment.getPaymentStatus());
            savedPayment.setTickets(inputPayment.getTickets());
            return savedPayment;
        });
        when(ticketMapper.mapTicketToTicketResponse(any(Ticket.class)))
                .thenAnswer(invocation -> {
                    Ticket ticket = invocation.getArgument(0);
                    TicketResponse response = new TicketResponse();
                    response.setSeatLetter(ticket.getSeatLetter());
                    response.setSeatNumber(ticket.getSeatNumber());
                    response.setPrice(ticket.getPrice());
                    return response;
                });

        // When
        ResponseMessage<List<TicketResponse>> result = ticketReservationService.reserveTicket(request);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject()).hasSize(1);

        verify(paymentRepository).save(argThat(savedPayment -> {
            assertThat(savedPayment.getAmount()).isEqualTo(15.0);
            assertThat(savedPayment.getTickets()).hasSize(1);

            Ticket ticket = savedPayment.getTickets().iterator().next();
            assertThat(ticket.getSeatLetter()).isEqualTo("B");
            assertThat(ticket.getSeatNumber()).isEqualTo(5);
            assertThat(ticket.getPrice()).isEqualTo(15.0); // Full price for single seat

            return true;
        }));
    }

    @Test
    void reserveTicket_PriceCalculation_MultipleSeats() {
        // Given
        List<SeatInfo> threeSeats = Arrays.asList(
                new SeatInfo("C", 1),
                new SeatInfo("C", 2),
                new SeatInfo("C", 3)
        );
        request.setSeatInfos(threeSeats);
        request.setTicketPrice(30.0);

        when(movieRepository.findByTitle("Test Movie")).thenReturn(Optional.of(movie));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(hallRepository.getReferenceById(1L)).thenReturn(hall);
        when(ticketRepository.findOccupiedSeatsByShowtimeAndStatus(1L,
                List.of(TicketStatus.PAID, TicketStatus.RESERVED))).thenReturn(List.of());
        when(securityService.getCurrentUser()).thenReturn(user);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment inputPayment = invocation.getArgument(0);
            Payment savedPayment = new Payment();
            savedPayment.setId(1L);
            savedPayment.setUser(inputPayment.getUser());
            savedPayment.setAmount(inputPayment.getAmount());
            savedPayment.setPaymentStatus(inputPayment.getPaymentStatus());
            savedPayment.setTickets(inputPayment.getTickets());
            return savedPayment;
        });
        when(ticketMapper.mapTicketToTicketResponse(any(Ticket.class)))
                .thenAnswer(invocation -> {
                    Ticket ticket = invocation.getArgument(0);
                    TicketResponse response = new TicketResponse();
                    response.setSeatLetter(ticket.getSeatLetter());
                    response.setSeatNumber(ticket.getSeatNumber());
                    response.setPrice(ticket.getPrice());
                    return response;
                });

        // When
        ResponseMessage<List<TicketResponse>> result = ticketReservationService.reserveTicket(request);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);

        verify(paymentRepository).save(argThat(savedPayment -> {
            assertThat(savedPayment.getAmount()).isEqualTo(30.0);
            assertThat(savedPayment.getTickets()).hasSize(3);

            // Each ticket should have price = 30.0 / 3 = 10.0
            savedPayment.getTickets().forEach(ticket -> {
                assertThat(ticket.getPrice()).isEqualTo(10.0);
            });

            return true;
        }));
    }

    // Edge case: Test with zero price (free tickets)
    @Test
    void reserveTicket_ZeroPrice() {
        // Given
        request.setTicketPrice(0.0);

        when(movieRepository.findByTitle("Test Movie")).thenReturn(Optional.of(movie));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(hallRepository.getReferenceById(1L)).thenReturn(hall);
        when(ticketRepository.findOccupiedSeatsByShowtimeAndStatus(1L,
                List.of(TicketStatus.PAID, TicketStatus.RESERVED))).thenReturn(List.of());
        when(securityService.getCurrentUser()).thenReturn(user);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment inputPayment = invocation.getArgument(0);
            Payment savedPayment = new Payment();
            savedPayment.setId(1L);
            savedPayment.setUser(inputPayment.getUser());
            savedPayment.setAmount(inputPayment.getAmount());
            savedPayment.setPaymentStatus(inputPayment.getPaymentStatus());
            savedPayment.setTickets(inputPayment.getTickets());
            return savedPayment;
        });
        when(ticketMapper.mapTicketToTicketResponse(any(Ticket.class)))
                .thenAnswer(invocation -> {
                    Ticket ticket = invocation.getArgument(0);
                    TicketResponse response = new TicketResponse();
                    response.setSeatLetter(ticket.getSeatLetter());
                    response.setSeatNumber(ticket.getSeatNumber());
                    response.setPrice(ticket.getPrice());
                    return response;
                });

        // When
        ResponseMessage<List<TicketResponse>> result = ticketReservationService.reserveTicket(request);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);

        verify(paymentRepository).save(argThat(savedPayment -> {
            assertThat(savedPayment.getAmount()).isEqualTo(0.0);
            savedPayment.getTickets().forEach(ticket -> {
                assertThat(ticket.getPrice()).isEqualTo(0.0);
            });
            return true;
        }));
    }
}