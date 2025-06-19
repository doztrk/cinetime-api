package com.Cinetime.service.ticketservice;

import com.Cinetime.payload.dto.request.TicketPriceCalculationRequest;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.repo.ShowtimeRepository;
import com.Cinetime.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketService - calculateTicketPrice Tests")
class CalculateTicketPriceTest {

    @Mock
    private ShowtimeRepository showtimeRepository;

    @InjectMocks
    private TicketService ticketService;

    private TicketPriceCalculationRequest validRequest;
    private TicketPriceCalculationRequest invalidShowtimeRequest;
    private Long validShowtimeId;
    private Long invalidShowtimeId;
    private Double showtimePrice;
    private List<TicketPriceCalculationRequest.SeatPosition> seatList;

    @BeforeEach
    void setUp() {
        validShowtimeId = 1L;
        invalidShowtimeId = 999L;
        showtimePrice = 15.0;

        seatList = Arrays.asList(
                new TicketPriceCalculationRequest.SeatPosition("A", 1),
                new TicketPriceCalculationRequest.SeatPosition("A", 2),
                new TicketPriceCalculationRequest.SeatPosition("B", 1)
        );

        validRequest = TicketPriceCalculationRequest.builder()
                .showtimeId(validShowtimeId)
                .seats(seatList)
                .build();

        invalidShowtimeRequest = TicketPriceCalculationRequest.builder()
                .showtimeId(invalidShowtimeId)
                .seats(seatList)
                .build();
    }

    @Test
    @DisplayName("Should calculate ticket price successfully when showtime exists")
    void calculateTicketPrice_WhenShowtimeExists_ShouldReturnCalculatedPrice() {
        // Given
        when(showtimeRepository.existsById(validShowtimeId)).thenReturn(true);
        when(showtimeRepository.findShowtimePriceByshowtimeId(validShowtimeId)).thenReturn(showtimePrice);

        // When
        ResponseMessage<Double> result = ticketService.calculateTicketPrice(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.TICKET_PRICE_CALCULATED_SUCCESSFULLY);
        assertThat(result.getObject()).isEqualTo(45.0); // 15.0 * 3 seats
    }

    @Test
    @DisplayName("Should return NOT_FOUND when showtime does not exist")
    void calculateTicketPrice_WhenShowtimeDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(showtimeRepository.existsById(invalidShowtimeId)).thenReturn(false);

        // When
        ResponseMessage<Double> result = ticketService.calculateTicketPrice(invalidShowtimeRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.SHOWTIME_NOT_FOUND);
        assertThat(result.getObject()).isNull();
    }

    @Test
    @DisplayName("Should calculate correct price for single seat")
    void calculateTicketPrice_WithSingleSeat_ShouldReturnCorrectPrice() {
        // Given
        List<TicketPriceCalculationRequest.SeatPosition> singleSeat =
                Collections.singletonList(new TicketPriceCalculationRequest.SeatPosition("A", 1));
        TicketPriceCalculationRequest singleSeatRequest = TicketPriceCalculationRequest.builder()
                .showtimeId(validShowtimeId)
                .seats(singleSeat)
                .build();

        when(showtimeRepository.existsById(validShowtimeId)).thenReturn(true);
        when(showtimeRepository.findShowtimePriceByshowtimeId(validShowtimeId)).thenReturn(showtimePrice);

        // When
        ResponseMessage<Double> result = ticketService.calculateTicketPrice(singleSeatRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.TICKET_PRICE_CALCULATED_SUCCESSFULLY);
        assertThat(result.getObject()).isEqualTo(15.0); // 15.0 * 1 seat
    }

    @Test
    @DisplayName("Should calculate correct price for multiple seats")
    void calculateTicketPrice_WithMultipleSeats_ShouldReturnCorrectPrice() {
        // Given
        List<TicketPriceCalculationRequest.SeatPosition> multipleSeats = Arrays.asList(
                new TicketPriceCalculationRequest.SeatPosition("A", 1),
                new TicketPriceCalculationRequest.SeatPosition("A", 2),
                new TicketPriceCalculationRequest.SeatPosition("A", 3),
                new TicketPriceCalculationRequest.SeatPosition("B", 1),
                new TicketPriceCalculationRequest.SeatPosition("B", 2)
        );
        TicketPriceCalculationRequest multipleSeatRequest = TicketPriceCalculationRequest.builder()
                .showtimeId(validShowtimeId)
                .seats(multipleSeats)
                .build();

        when(showtimeRepository.existsById(validShowtimeId)).thenReturn(true);
        when(showtimeRepository.findShowtimePriceByshowtimeId(validShowtimeId)).thenReturn(showtimePrice);

        // When
        ResponseMessage<Double> result = ticketService.calculateTicketPrice(multipleSeatRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.TICKET_PRICE_CALCULATED_SUCCESSFULLY);
        assertThat(result.getObject()).isEqualTo(75.0); // 15.0 * 5 seats
    }

    @Test
    @DisplayName("Should handle zero price from repository")
    void calculateTicketPrice_WithZeroPrice_ShouldReturnZeroTotal() {
        // Given
        when(showtimeRepository.existsById(validShowtimeId)).thenReturn(true);
        when(showtimeRepository.findShowtimePriceByshowtimeId(validShowtimeId)).thenReturn(0.0);

        // When
        ResponseMessage<Double> result = ticketService.calculateTicketPrice(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.TICKET_PRICE_CALCULATED_SUCCESSFULLY);
        assertThat(result.getObject()).isEqualTo(0.0); // 0.0 * 3 seats
    }
}