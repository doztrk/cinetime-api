package com.Cinetime.service.showtimeservice;

import com.Cinetime.entity.Showtime;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.dto.response.ShowtimeResponse;
import com.Cinetime.payload.mappers.ShowtimeMapper;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.repo.ShowtimeRepository;
import com.Cinetime.service.ShowtimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetShowtimeByIdTest {

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private ShowtimeMapper showtimeMapper;

    @InjectMocks
    private ShowtimeService showtimeService;

    private static final Long VALID_SHOWTIME_ID = 1L;
    private static final Long INVALID_SHOWTIME_ID = 999L;

    private Showtime mockShowtime;
    private ShowtimeResponse mockShowtimeResponse;

    @BeforeEach
    void setUp() {
        mockShowtime = new Showtime();
        mockShowtime.setId(VALID_SHOWTIME_ID);

        mockShowtimeResponse = ShowtimeResponse.builder()
                .id(VALID_SHOWTIME_ID)
                .build();
    }

    @Test
    void getShowtimeById_WhenShowtimeExists_ShouldReturnSuccess() {
        // Arrange
        when(showtimeRepository.findById(VALID_SHOWTIME_ID))
                .thenReturn(Optional.of(mockShowtime));
        when(showtimeMapper.mapShowtimeToShowtimeResponse(mockShowtime))
                .thenReturn(mockShowtimeResponse);

        // Act
        ResponseMessage<ShowtimeResponse> result = showtimeService.getShowtimeById(VALID_SHOWTIME_ID);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getHttpStatus());
        assertEquals(SuccessMessages.SHOWTIME_FOUND, result.getMessage());
        assertNotNull(result.getObject());
        assertEquals(mockShowtimeResponse, result.getObject());

        // Verify interactions
        verify(showtimeRepository, times(1)).findById(VALID_SHOWTIME_ID);
        verify(showtimeMapper, times(1)).mapShowtimeToShowtimeResponse(mockShowtime);
        verifyNoMoreInteractions(showtimeRepository, showtimeMapper);
    }

    @Test
    void getShowtimeById_WhenShowtimeDoesNotExist_ShouldReturnNotFound() {
        // Arrange
        when(showtimeRepository.findById(INVALID_SHOWTIME_ID))
                .thenReturn(Optional.empty());

        // Act
        ResponseMessage<ShowtimeResponse> result = showtimeService.getShowtimeById(INVALID_SHOWTIME_ID);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getHttpStatus());
        assertEquals(ErrorMessages.SHOWTIME_NOT_FOUND, result.getMessage());
        assertNull(result.getObject());

        // Verify interactions - mapper should NOT be called when showtime doesn't exist
        verify(showtimeRepository, times(1)).findById(INVALID_SHOWTIME_ID);
        verify(showtimeMapper, never()).mapShowtimeToShowtimeResponse(any());
        verifyNoMoreInteractions(showtimeRepository, showtimeMapper);
    }

    @Test
    void getShowtimeById_WhenNullIdProvided_ShouldHandleGracefully() {
        // Arrange
        when(showtimeRepository.findById(null))
                .thenReturn(Optional.empty());

        // Act
        ResponseMessage<ShowtimeResponse> result = showtimeService.getShowtimeById(null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getHttpStatus());
        assertEquals(ErrorMessages.SHOWTIME_NOT_FOUND, result.getMessage());
        assertNull(result.getObject());

        // Verify
        verify(showtimeRepository, times(1)).findById(null);
        verify(showtimeMapper, never()).mapShowtimeToShowtimeResponse(any());
    }

    @Test
    void getShowtimeById_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Database connection failed");
        when(showtimeRepository.findById(VALID_SHOWTIME_ID))
                .thenThrow(expectedException);

        // Act & Assert
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            showtimeService.getShowtimeById(VALID_SHOWTIME_ID);
        });

        assertEquals("Database connection failed", thrownException.getMessage());

        // Verify
        verify(showtimeRepository, times(1)).findById(VALID_SHOWTIME_ID);
        verify(showtimeMapper, never()).mapShowtimeToShowtimeResponse(any());
    }

    @Test
    void getShowtimeById_WhenMapperThrowsException_ShouldPropagateException() {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Mapping failed");
        when(showtimeRepository.findById(VALID_SHOWTIME_ID))
                .thenReturn(Optional.of(mockShowtime));
        when(showtimeMapper.mapShowtimeToShowtimeResponse(mockShowtime))
                .thenThrow(expectedException);

        // Act & Assert
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            showtimeService.getShowtimeById(VALID_SHOWTIME_ID);
        });

        assertEquals("Mapping failed", thrownException.getMessage());

        // Verify both services were called before exception
        verify(showtimeRepository, times(1)).findById(VALID_SHOWTIME_ID);
        verify(showtimeMapper, times(1)).mapShowtimeToShowtimeResponse(mockShowtime);
    }
}