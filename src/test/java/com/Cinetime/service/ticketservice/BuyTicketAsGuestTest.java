package com.Cinetime.service.ticketservice;

import com.Cinetime.entity.*;
import com.Cinetime.enums.PaymentStatus;
import com.Cinetime.enums.TicketStatus;
import com.Cinetime.payload.business.SeatInfo;
import com.Cinetime.payload.dto.request.GuestInfoRequest;
import com.Cinetime.payload.dto.request.TicketPurchaseGuestRequest;
import com.Cinetime.payload.dto.response.AnonymousTicketResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.dto.response.TicketResponse;
import com.Cinetime.payload.mappers.TicketMapper;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.repo.*;
import com.Cinetime.service.EmailService;
import com.Cinetime.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketService - buyTicketsAsGuest Tests")
class BuyTicketAsGuestTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private HallRepository hallRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private AnonymousUserRepository anonymousUserRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private TicketMapper ticketMapper;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private TicketService ticketService;

    private TicketPurchaseGuestRequest validRequest;
    private Movie testMovie;
    private Showtime testShowtime;
    private Hall testHall;
    private Cinema testCinema;
    private District testDistrict;
    private Country testCountry;
    private City testCity;
    private AnonymousUser testAnonymousUser;
    private GuestInfoRequest guestInfo;
    private List<SeatInfo> seatInfos;
    private AnonymousTicketResponse mockTicketResponse;

    @BeforeEach
    void setUp() {
        // Create Country first
        Country testCountry = new Country();
        testCountry.setId(1L);
        testCountry.setName("Turkey");

        // Create City
        testCity = new City();
        testCity.setId(1L);
        testCity.setName("Istanbul");
        testCity.setCountry(testCountry);

        // Create District
        testDistrict = new District();
        testDistrict.setId(1L);
        testDistrict.setName("Kadikoy");
        testDistrict.setCity(testCity);

        // Create Cinema with proper relationships
        testCinema = new Cinema();
        testCinema.setId(1L);
        testCinema.setName("Test Cinema");
        testCinema.setSlug("test-cinema");
        testCinema.setAddress("Test Address");
        testCinema.setPhone("1234567890");
        testCinema.setEmail("cinema@test.com");
        testCinema.setDistrict(testDistrict);
        testCinema.setCity(testCity);
        testCinema.setCreatedAt(LocalDateTime.now());
        testCinema.setUpdatedAt(LocalDateTime.now());

        // Create Hall with Cinema relationship
        testHall = new Hall();
        testHall.setId(1L);
        testHall.setName("Hall A");
        testHall.setSeatCapacity(100);
        testHall.setIsSpecial(false);
        testHall.setCinema(testCinema); // This is crucial!
        testHall.setCreatedAt(LocalDateTime.now());
        testHall.setUpdatedAt(LocalDateTime.now());

        testMovie = new Movie();
        testMovie.setId(1L);
        testMovie.setTitle("Test Movie");

        testShowtime = new Showtime();
        testShowtime.setId(1L);
        testShowtime.setHall(testHall);
        testShowtime.setDate(LocalDate.now().plusDays(1)); // Set a future date
        testShowtime.setStartTime(LocalTime.of(20, 0)); // 8:00 PM
        testShowtime.setEndTime(LocalTime.of(22, 30)); // 10:30 PM
        testShowtime.setCreatedAt(LocalDateTime.now());
        testShowtime.setUpdatedAt(LocalDateTime.now());

        guestInfo = new GuestInfoRequest();
        guestInfo.setEmail("test@example.com");
        guestInfo.setFullName("John Doe");
        guestInfo.setPhoneNumber("1234567890");

        SeatInfo seat1 = new SeatInfo();
        seat1.setSeatLetter("A");
        seat1.setSeatNumber(1);

        SeatInfo seat2 = new SeatInfo();
        seat2.setSeatLetter("A");
        seat2.setSeatNumber(2);

        seatInfos = Arrays.asList(seat1, seat2);

        validRequest = new TicketPurchaseGuestRequest();
        validRequest.setMovieName("Test Movie");
        validRequest.setShowtimeId(1L);
        validRequest.setTicketPrice(100.0); // Total price for 2 tickets
        validRequest.setSeatInfos(seatInfos);
        validRequest.setAnonymousUser(guestInfo);

        testAnonymousUser = new AnonymousUser();
        testAnonymousUser.setId(1L);
        testAnonymousUser.setEmail("test@example.com");
        testAnonymousUser.setFullName("John Doe");
        testAnonymousUser.setPhoneNumber("1234567890");
        testAnonymousUser.setRetrievalCode("test-retrieval-code");

        TicketResponse ticketResponse = new TicketResponse();
        ticketResponse.setId(1L);

        mockTicketResponse = new AnonymousTicketResponse();
        mockTicketResponse.setRetrievalId("test-retrieval-code");
        mockTicketResponse.setTicketResponse(ticketResponse);
    }

    @Test
    @DisplayName("Should successfully buy tickets as guest when all validations pass")
    void buyTicketsAsGuest_Success() {
        // Given
        when(movieRepository.findByTitle("Test Movie")).thenReturn(Optional.of(testMovie));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(testShowtime));
        when(hallRepository.findById(1L)).thenReturn(Optional.of(testHall));
        when(ticketRepository.findOccupiedSeatsByShowtimeAndStatus(1L, List.of(TicketStatus.PAID, TicketStatus.RESERVED)))
                .thenReturn(Collections.emptyList());

        // Mock the anonymous user save to return our test user
        when(anonymousUserRepository.save(any(AnonymousUser.class))).thenReturn(testAnonymousUser);

        // Create and mock the payment save
        Payment savedPayment = createMockPayment();
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Mock the ticket mapper
        when(ticketMapper.mapTicketToAnonymousTicketResponse(any(Ticket.class), anyString()))
                .thenReturn(mockTicketResponse);

        // Mock email service to do nothing
        doNothing().when(emailService).sendMail(any());

        // When
        ResponseMessage<List<AnonymousTicketResponse>> result = ticketService.buyTicketsAsGuest(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.TICKET_BOUGHT_SUCCESSFULLY);
        assertThat(result.getObject()).hasSize(2);

        // Verify repository interactions
        verify(movieRepository).findByTitle("Test Movie");
        verify(showtimeRepository).findById(1L);
        verify(hallRepository).findById(1L);
        verify(ticketRepository).findOccupiedSeatsByShowtimeAndStatus(1L, List.of(TicketStatus.PAID, TicketStatus.RESERVED));
        verify(anonymousUserRepository).save(any(AnonymousUser.class));
        verify(paymentRepository).save(any(Payment.class));

        // Verify anonymous user creation
        ArgumentCaptor<AnonymousUser> userCaptor = ArgumentCaptor.forClass(AnonymousUser.class);
        verify(anonymousUserRepository).save(userCaptor.capture());
        AnonymousUser savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getFullName()).isEqualTo("John Doe");
        assertThat(savedUser.getPhoneNumber()).isEqualTo("1234567890");
        assertThat(savedUser.getRetrievalCode()).isNotNull();

        // Verify email service was called
        verify(emailService).sendMail(any());
    }

    @Test
    @DisplayName("Should return NOT_FOUND when movie does not exist")
    void buyTicketsAsGuest_MovieNotFound() {
        // Given
        when(movieRepository.findByTitle("Non-existent Movie")).thenReturn(Optional.empty());

        validRequest.setMovieName("Non-existent Movie");

        // When
        ResponseMessage<List<AnonymousTicketResponse>> result = ticketService.buyTicketsAsGuest(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.MOVIE_NOT_FOUND);
        assertThat(result.getObject()).isNull();

        verify(movieRepository).findByTitle("Non-existent Movie");
        verifyNoInteractions(showtimeRepository, hallRepository, ticketRepository,
                anonymousUserRepository, paymentRepository);
    }

    @Test
    @DisplayName("Should return NOT_FOUND when showtime does not exist")
    void buyTicketsAsGuest_ShowtimeNotFound() {
        // Given
        when(movieRepository.findByTitle("Test Movie")).thenReturn(Optional.of(testMovie));
        when(showtimeRepository.findById(999L)).thenReturn(Optional.empty());

        validRequest.setShowtimeId(999L);

        // When
        ResponseMessage<List<AnonymousTicketResponse>> result = ticketService.buyTicketsAsGuest(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.SHOWTIME_NOT_FOUND);
        assertThat(result.getObject()).isNull();

        verify(movieRepository).findByTitle("Test Movie");
        verify(showtimeRepository).findById(999L);
        verifyNoInteractions(hallRepository, ticketRepository, anonymousUserRepository, paymentRepository);
    }

    @Test
    @DisplayName("Should return NOT_FOUND when hall does not exist")
    void buyTicketsAsGuest_HallNotFound() {
        // Given
        when(movieRepository.findByTitle("Test Movie")).thenReturn(Optional.of(testMovie));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(testShowtime));
        when(hallRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        ResponseMessage<List<AnonymousTicketResponse>> result = ticketService.buyTicketsAsGuest(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.HALL_NOT_FOUND);
        assertThat(result.getObject()).isNull();

        verify(movieRepository).findByTitle("Test Movie");
        verify(showtimeRepository).findById(1L);
        verify(hallRepository).findById(1L);
        verifyNoInteractions(ticketRepository, anonymousUserRepository, paymentRepository);
    }

    @Test
    @DisplayName("Should return CONFLICT when seats are already occupied")
    void buyTicketsAsGuest_SeatsOccupied() {
        // Given
        when(movieRepository.findByTitle("Test Movie")).thenReturn(Optional.of(testMovie));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(testShowtime));
        when(hallRepository.findById(1L)).thenReturn(Optional.of(testHall));
        when(ticketRepository.findOccupiedSeatsByShowtimeAndStatus(1L, List.of(TicketStatus.PAID, TicketStatus.RESERVED)))
                .thenReturn(Arrays.asList("A1", "A2"));

        // When
        ResponseMessage<List<AnonymousTicketResponse>> result = ticketService.buyTicketsAsGuest(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.SEATS_ARE_OCCUPIED);
        assertThat(result.getObject()).isNull();

        verify(movieRepository).findByTitle("Test Movie");
        verify(showtimeRepository).findById(1L);
        verify(hallRepository).findById(1L);
        verify(ticketRepository).findOccupiedSeatsByShowtimeAndStatus(1L, List.of(TicketStatus.PAID, TicketStatus.RESERVED));
        verifyNoInteractions(anonymousUserRepository, paymentRepository);
    }

    @Test
    @DisplayName("Should return INTERNAL_SERVER_ERROR when email sending fails ")
    void buyTicketsAsGuest_EmailSendingFails_ShouldReturnInternalServerError() {
        // Given
        when(movieRepository.findByTitle("Test Movie")).thenReturn(Optional.of(testMovie));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(testShowtime));
        when(hallRepository.findById(1L)).thenReturn(Optional.of(testHall));
        when(ticketRepository.findOccupiedSeatsByShowtimeAndStatus(1L, List.of(TicketStatus.PAID, TicketStatus.RESERVED)))
                .thenReturn(Collections.emptyList());
        when(anonymousUserRepository.save(any(AnonymousUser.class))).thenReturn(testAnonymousUser);

        Payment savedPayment = createMockPayment();
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(ticketMapper.mapTicketToAnonymousTicketResponse(any(Ticket.class), anyString()))
                .thenReturn(mockTicketResponse);

        // Mock email service to throw exception
        doThrow(new RuntimeException("Email service failed")).when(emailService).sendMail(any());

        // When
        ResponseMessage<List<AnonymousTicketResponse>> result = ticketService.buyTicketsAsGuest(validRequest);

        // Then


        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getMessage()).contains("Failed to send email");
        assertThat(result.getObject()).isNull();

        // Despite the error response, verify that the payment was actually processed
        verify(paymentRepository).save(any(Payment.class));
        verify(anonymousUserRepository).save(any(AnonymousUser.class));

        // This proves the business logic flaw: customer is charged but gets error message
        // This needs to be fixed at the service level!
    }

    @Test
    @DisplayName("Should correctly calculate price per ticket")
    void buyTicketsAsGuest_CorrectPriceCalculation() {
        // Given
        validRequest.setTicketPrice(100.0); // Total price for 2 tickets

        when(movieRepository.findByTitle("Test Movie")).thenReturn(Optional.of(testMovie));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(testShowtime));
        when(hallRepository.findById(1L)).thenReturn(Optional.of(testHall));
        when(ticketRepository.findOccupiedSeatsByShowtimeAndStatus(1L, List.of(TicketStatus.PAID, TicketStatus.RESERVED)))
                .thenReturn(Collections.emptyList());
        when(anonymousUserRepository.save(any(AnonymousUser.class))).thenReturn(testAnonymousUser);

        Payment savedPayment = createMockPayment();
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(ticketMapper.mapTicketToAnonymousTicketResponse(any(Ticket.class), anyString()))
                .thenReturn(mockTicketResponse);
        doNothing().when(emailService).sendMail(any());

        // When
        ResponseMessage<List<AnonymousTicketResponse>> result = ticketService.buyTicketsAsGuest(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);

        // Verify payment creation with correct price calculation
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment savedPaymentArg = paymentCaptor.getValue();

        // Each ticket should be 50.0 (100.0 / 2 tickets)
        savedPaymentArg.getTickets().forEach(ticket -> {
            assertThat(ticket.getPrice()).isEqualTo(50.0);
        });

        verify(emailService).sendMail(any());
    }

    @Test
    @DisplayName("Should handle partial seat occupancy correctly")
    void buyTicketsAsGuest_PartialSeatOccupancy() {
        // Given
        when(movieRepository.findByTitle("Test Movie")).thenReturn(Optional.of(testMovie));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(testShowtime));
        when(hallRepository.findById(1L)).thenReturn(Optional.of(testHall));
        when(ticketRepository.findOccupiedSeatsByShowtimeAndStatus(1L, List.of(TicketStatus.PAID, TicketStatus.RESERVED)))
                .thenReturn(Arrays.asList("A1")); // Only A1 is occupied

        // When
        ResponseMessage<List<AnonymousTicketResponse>> result = ticketService.buyTicketsAsGuest(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.SEATS_ARE_OCCUPIED);
    }

    private Payment createMockPayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setAmount(100.0); // Total amount
        payment.setPaymentStatus(PaymentStatus.PENDING); // *** IMPORTANT: Service sets PENDING, not SUCCESS ***
        payment.setAnonymousUser(testAnonymousUser);

        Set<Ticket> tickets = new HashSet<>();
        for (SeatInfo seatInfo : seatInfos) {
            Ticket ticket = Ticket.builder()
                    .id((long) tickets.size() + 1)
                    .movie(testMovie)
                    .showtime(testShowtime)
                    .anonymousUser(testAnonymousUser)
                    .hall(testHall)
                    .seatLetter(seatInfo.getSeatLetter())
                    .seatNumber(seatInfo.getSeatNumber())
                    .price(50.0) // 100.0 / 2 tickets = 50.0 each
                    .status(TicketStatus.PAID) // Tickets are marked as PAID
                    .payment(payment)
                    .build();
            tickets.add(ticket);
        }
        payment.setTickets(tickets);

        return payment;
    }
}