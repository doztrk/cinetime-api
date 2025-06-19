package com.Cinetime.service.ticketservice;

import com.Cinetime.payload.dto.response.ResponseMessage;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketService - getTicketPrice Tests")
class GetTicketPriceTest {

    @Mock
    private ShowtimeRepository showtimeRepository;

    @InjectMocks
    private TicketService ticketService;

    private Long validShowtimeId;
    private Long invalidShowtimeId;
    private Double expectedPrice;

    @BeforeEach
    void setUp() {
        validShowtimeId = 1L;
        invalidShowtimeId = 999L;
        expectedPrice = 15.50;
    }

    @Test
    @DisplayName("Should return ticket price successfully when showtime exists")
    void getTicketPrice_WhenShowtimeExists_ShouldReturnPriceSuccessfully() {
        // Given
        when(showtimeRepository.findShowtimePriceByshowtimeId(validShowtimeId)).thenReturn(expectedPrice);

        // When
        ResponseMessage<Double> result = ticketService.getTicketPrice(validShowtimeId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.TICKET_PRICE_FOUND_SUCCESSFULLY);
        assertThat(result.getObject()).isEqualTo(expectedPrice);

        verify(showtimeRepository, times(1)).findShowtimePriceByshowtimeId(validShowtimeId);
        verifyNoMoreInteractions(showtimeRepository);
    }

    @Test
    @DisplayName("Should handle null price when showtime does not exist")
    void getTicketPrice_WhenShowtimeDoesNotExist_ShouldReturnNullPrice() {
        // Given
        when(showtimeRepository.findShowtimePriceByshowtimeId(invalidShowtimeId)).thenReturn(null);

        // When
        ResponseMessage<Double> result = ticketService.getTicketPrice(invalidShowtimeId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.TICKET_PRICE_FOUND_SUCCESSFULLY);
        assertThat(result.getObject()).isNull();

        verify(showtimeRepository, times(1)).findShowtimePriceByshowtimeId(invalidShowtimeId);
        verifyNoMoreInteractions(showtimeRepository);
    }

    @Test
    @DisplayName("Should handle different price values correctly")
    void getTicketPrice_WithDifferentPriceValues_ShouldReturnCorrectPrices() {
        // Given
        Double highPrice = 25.75;
        Long showtimeId = 2L;
        when(showtimeRepository.findShowtimePriceByshowtimeId(showtimeId)).thenReturn(highPrice);

        // When
        ResponseMessage<Double> result = ticketService.getTicketPrice(showtimeId);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject()).isEqualTo(highPrice);
        assertThat(result.getObject()).isNotEqualTo(expectedPrice);
    }

    @Test
    @DisplayName("Should handle zero price correctly")
    void getTicketPrice_WhenPriceIsZero_ShouldReturnZeroPrice() {
        // Given
        Double zeroPrice = 0.0;
        when(showtimeRepository.findShowtimePriceByshowtimeId(validShowtimeId)).thenReturn(zeroPrice);

        // When
        ResponseMessage<Double> result = ticketService.getTicketPrice(validShowtimeId);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject()).isEqualTo(zeroPrice);
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.TICKET_PRICE_FOUND_SUCCESSFULLY);
    }

    @Test
    @DisplayName("Should maintain ResponseMessage structure consistency")
    void getTicketPrice_ShouldMaintainResponseMessageStructureConsistency() {
        // Given
        when(showtimeRepository.findShowtimePriceByshowtimeId(validShowtimeId)).thenReturn(expectedPrice);

        // When
        ResponseMessage<Double> result = ticketService.getTicketPrice(validShowtimeId);

        // Then - Verify ResponseMessage structure
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isNotNull().isNotEmpty();
        assertThat(result.getHttpStatus()).isNotNull();
        assertThat(result.getObject()).isNotNull();

        // Verify specific type
        assertThat(result.getObject()).isInstanceOf(Double.class);
    }
}