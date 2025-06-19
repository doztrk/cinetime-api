package com.Cinetime.service.seatservice;

import com.Cinetime.enums.TicketStatus;
import com.Cinetime.payload.business.SeatInfo;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.dto.response.SeatResponse;
import com.Cinetime.repo.TicketRepository;
import com.Cinetime.payload.mappers.SeatMapper;
import com.Cinetime.service.SeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private SeatMapper seatMapper;

    @InjectMocks
    private SeatService seatService; // Assuming your service class name

    private Long validShowtimeId;
    private List<SeatInfo> mockSeatInfoList;
    private List<SeatResponse> mockSeatResponseList;

    @BeforeEach
    void setUp() {
        validShowtimeId = 1L;

        // Create mock SeatInfo objects
        mockSeatInfoList = Arrays.asList(
                SeatInfo.builder()
                        .seatLetter("A")
                        .seatNumber(1)
                        .build(),
                SeatInfo.builder()
                        .seatLetter("A")
                        .seatNumber(2)
                        .build(),
                SeatInfo.builder()
                        .seatLetter("B")
                        .seatNumber(1)
                        .build()
        );

        // Create mock SeatResponse objects
        mockSeatResponseList = Arrays.asList(
                SeatResponse.builder()
                        .seatLetter("A")
                        .seatNumber(1)
                        .fullSeatName("A1")
                        .build(),
                SeatResponse.builder()
                        .seatLetter("A")
                        .seatNumber(2)
                        .fullSeatName("A2")
                        .build(),
                SeatResponse.builder()
                        .seatLetter("B")
                        .seatNumber(1)
                        .fullSeatName("B1")
                        .build()
        );
    }

    @Test
    void getOccupiedSeats_WithValidShowtimeId_ShouldReturnOccupiedSeats() {
        // Arrange
        when(ticketRepository.findOccupiedSeatInfoByShowtimeAndStatus(
                eq(validShowtimeId),
                eq(List.of(TicketStatus.PAID, TicketStatus.RESERVED))
        )).thenReturn(mockSeatInfoList);

        when(seatMapper.mapSeatInfoListToSeatResponseList(mockSeatInfoList))
                .thenReturn(mockSeatResponseList);

        // Act
        ResponseMessage<List<SeatResponse>> result = seatService.getOccupiedSeats(validShowtimeId);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getHttpStatus());
        assertEquals("Reserved seats found successfully", result.getMessage());
        assertEquals(mockSeatResponseList, result.getObject());
        assertEquals(3, result.getObject().size());

        // Verify interactions
        verify(ticketRepository, times(1))
                .findOccupiedSeatInfoByShowtimeAndStatus(validShowtimeId, List.of(TicketStatus.PAID, TicketStatus.RESERVED));
        verify(seatMapper, times(1))
                .mapSeatInfoListToSeatResponseList(mockSeatInfoList);
    }

    @Test
    void getOccupiedSeats_WithNoOccupiedSeats_ShouldReturnEmptyList() {
        // Arrange
        when(ticketRepository.findOccupiedSeatInfoByShowtimeAndStatus(
                eq(validShowtimeId),
                eq(List.of(TicketStatus.PAID, TicketStatus.RESERVED))
        )).thenReturn(Collections.emptyList());

        when(seatMapper.mapSeatInfoListToSeatResponseList(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseMessage<List<SeatResponse>> result = seatService.getOccupiedSeats(validShowtimeId);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getHttpStatus());
        assertEquals("Reserved seats found successfully", result.getMessage());
        assertTrue(result.getObject().isEmpty());

        verify(ticketRepository, times(1))
                .findOccupiedSeatInfoByShowtimeAndStatus(validShowtimeId, List.of(TicketStatus.PAID, TicketStatus.RESERVED));
        verify(seatMapper, times(1))
                .mapSeatInfoListToSeatResponseList(Collections.emptyList());
    }

    @Test
    void getOccupiedSeats_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Database connection failed");
        when(ticketRepository.findOccupiedSeatInfoByShowtimeAndStatus(any(), any()))
                .thenThrow(expectedException);

        // Act & Assert
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            seatService.getOccupiedSeats(validShowtimeId);
        });

        assertEquals("Database connection failed", thrownException.getMessage());
        verify(seatMapper, never()).mapSeatInfoListToSeatResponseList(any());
    }

    @Test
    void getOccupiedSeats_WhenMapperThrowsException_ShouldPropagateException() {
        // Arrange
        when(ticketRepository.findOccupiedSeatInfoByShowtimeAndStatus(any(), any()))
                .thenReturn(mockSeatInfoList);

        RuntimeException expectedException = new RuntimeException("Mapping failed");
        when(seatMapper.mapSeatInfoListToSeatResponseList(any()))
                .thenThrow(expectedException);

        // Act & Assert
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            seatService.getOccupiedSeats(validShowtimeId);
        });

        assertEquals("Mapping failed", thrownException.getMessage());
    }

    @Test
    void getOccupiedSeats_WithNullShowtimeId_ShouldHandleGracefully() {
        // This test exposes the lack of null validation in your current code
        // Your method should validate this parameter

        // Act & Assert - This will likely cause NullPointerException in repository call
        assertThrows(Exception.class, () -> {
            seatService.getOccupiedSeats(null);
        });
    }

    @Test
    void getOccupiedSeats_VerifyCorrectStatusesArePassed() {
        // Arrange
        when(ticketRepository.findOccupiedSeatInfoByShowtimeAndStatus(any(), any()))
                .thenReturn(Collections.emptyList());
        when(seatMapper.mapSeatInfoListToSeatResponseList(any()))
                .thenReturn(Collections.emptyList());

        // Act
        seatService.getOccupiedSeats(validShowtimeId);

        // Assert - Verify the exact statuses being passed
        List<TicketStatus> expectedStatuses = List.of(TicketStatus.PAID, TicketStatus.RESERVED);
        verify(ticketRepository, times(1))
                .findOccupiedSeatInfoByShowtimeAndStatus(validShowtimeId, expectedStatuses);
    }
}