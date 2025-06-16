package com.Cinetime.service.showtimeservice;

import com.Cinetime.repo.ShowtimeRepository;
import com.Cinetime.service.ShowtimeService;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShowtimeService - showtimeUpdateCheck Tests")
class ShowtimeUpdateCheckTest {

    @Mock
    private ShowtimeRepository showtimeRepository;

    @InjectMocks
    private ShowtimeService showtimeService;

    @Test
    @DisplayName("Should not throw exception when no conflict exists")
    void showtimeUpdateCheck_NoConflict_ShouldNotThrowException() {
        // Arrange
        Long showtimeId = 1L;
        Long hallId = 2L;
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(14, 0);
        LocalTime endTime = LocalTime.of(16, 30);

        when(showtimeRepository.existsConflictForUpdate(showtimeId, hallId, date, startTime, endTime))
                .thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> {
            showtimeService.showtimeUpdateCheck(showtimeId, hallId, date, startTime, endTime);
        });

        verify(showtimeRepository).existsConflictForUpdate(showtimeId, hallId, date, startTime, endTime);
    }

    @Test
    @DisplayName("Should throw BadRequestException when conflict exists")
    void showtimeUpdateCheck_ConflictExists_ShouldThrowBadRequestException() {
        // Arrange
        Long showtimeId = 1L;
        Long hallId = 2L;
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(14, 0);
        LocalTime endTime = LocalTime.of(16, 30);

        when(showtimeRepository.existsConflictForUpdate(showtimeId, hallId, date, startTime, endTime))
                .thenReturn(true);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            showtimeService.showtimeUpdateCheck(showtimeId, hallId, date, startTime, endTime);
        });

        assertEquals("Bu saat aralığında bu salonda başka bir gösterim mevcut.", exception.getMessage());
        verify(showtimeRepository).existsConflictForUpdate(showtimeId, hallId, date, startTime, endTime);
    }

    @Test
    @DisplayName("Should handle edge case with same start and end times")
    void showtimeUpdateCheck_SameStartEndTime_ShouldCallRepository() {
        // Arrange
        Long showtimeId = 1L;
        Long hallId = 2L;
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(14, 0);
        LocalTime endTime = LocalTime.of(14, 0); // Same as start time

        when(showtimeRepository.existsConflictForUpdate(showtimeId, hallId, date, startTime, endTime))
                .thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> {
            showtimeService.showtimeUpdateCheck(showtimeId, hallId, date, startTime, endTime);
        });

        verify(showtimeRepository).existsConflictForUpdate(showtimeId, hallId, date, startTime, endTime);
    }

    @Test
    @DisplayName("Should handle boundary values correctly")
    void showtimeUpdateCheck_BoundaryValues_ShouldCallRepository() {
        // Arrange
        Long showtimeId = Long.MAX_VALUE;
        Long hallId = Long.MIN_VALUE;
        LocalDate date = LocalDate.now();
        LocalTime startTime = LocalTime.MIN;
        LocalTime endTime = LocalTime.MAX;

        when(showtimeRepository.existsConflictForUpdate(showtimeId, hallId, date, startTime, endTime))
                .thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> {
            showtimeService.showtimeUpdateCheck(showtimeId, hallId, date, startTime, endTime);
        });

        verify(showtimeRepository).existsConflictForUpdate(showtimeId, hallId, date, startTime, endTime);
    }

    @Test
    @DisplayName("Should verify exact method call with all parameters")
    void showtimeUpdateCheck_ShouldCallRepositoryWithExactParameters() {
        // Arrange
        Long showtimeId = 123L;
        Long hallId = 456L;
        LocalDate date = LocalDate.of(2025, 12, 25);
        LocalTime startTime = LocalTime.of(20, 30);
        LocalTime endTime = LocalTime.of(23, 15);

        when(showtimeRepository.existsConflictForUpdate(showtimeId, hallId, date, startTime, endTime))
                .thenReturn(false);

        // Act
        assertDoesNotThrow(() -> {
            showtimeService.showtimeUpdateCheck(showtimeId, hallId, date, startTime, endTime);
        });

        // Assert - Verify exact parameters were passed
        verify(showtimeRepository, times(1)).existsConflictForUpdate(
                eq(123L),
                eq(456L),
                eq(LocalDate.of(2025, 12, 25)),
                eq(LocalTime.of(20, 30)),
                eq(LocalTime.of(23, 15))
        );
        verifyNoMoreInteractions(showtimeRepository);
    }
}