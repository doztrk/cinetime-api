package com.Cinetime.service.hallservice;

import com.Cinetime.entity.Hall;
import com.Cinetime.payload.dto.response.HallResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.mappers.HallMapper;
import com.Cinetime.repo.HallRepository;
import com.Cinetime.service.HallService;
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
class GetHallByIdTest {

    @Mock
    private HallRepository hallRepository;

    @Mock
    private HallMapper hallMapper;

    @InjectMocks
    private HallService hallService;

    private Hall testHall;
    private HallResponse expectedResponse;

    @BeforeEach
    void setUp() {
        testHall = Hall.builder()
                .id(1L)
                .name("Test Hall")
                .seatCapacity(100)
                .build();

        expectedResponse = HallResponse.builder()
                .id(1L)
                .name("Test Hall")
                .seatCapacity(100)
                .build();
    }

    @Test
    void getHallById_WhenHallExists_ShouldReturnSuccessResponse() {
        // Given
        Long hallId = 1L;
        when(hallRepository.findById(hallId)).thenReturn(Optional.of(testHall));
        when(hallMapper.mapToHallResponse(testHall)).thenReturn(expectedResponse);

        // When
        ResponseMessage<HallResponse> result = hallService.getHallById(hallId);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getHttpStatus());
        assertEquals("Hall found successfully", result.getMessage());
        assertNotNull(result.getObject());
        assertEquals(expectedResponse.getId(), result.getObject().getId());
        assertEquals(expectedResponse.getName(), result.getObject().getName());
        assertEquals(expectedResponse.getSeatCapacity(), result.getObject().getSeatCapacity());

        verify(hallRepository, times(1)).findById(hallId);
        verify(hallMapper, times(1)).mapToHallResponse(testHall);
    }

    @Test
    void getHallById_WhenHallNotFound_ShouldReturnNotFoundResponse() {
        // Given
        Long hallId = 999L;
        when(hallRepository.findById(hallId)).thenReturn(Optional.empty());

        // When
        ResponseMessage<HallResponse> result = hallService.getHallById(hallId);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getHttpStatus());
        assertEquals("Hall not found", result.getMessage());
        assertNull(result.getObject());

        verify(hallRepository, times(1)).findById(hallId);
    }

    @Test
    void getHallById_WhenNullId_ShouldHandleGracefully() {
        // Given
        Long hallId = null;
        when(hallRepository.findById(null)).thenReturn(Optional.empty());

        // When
        ResponseMessage<HallResponse> result = hallService.getHallById(hallId);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getHttpStatus());
        assertEquals("Hall not found", result.getMessage());
        assertNull(result.getObject());

        verify(hallRepository, times(1)).findById(null);
    }

    @Test
    void getHallById_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Given
        Long hallId = 1L;
        when(hallRepository.findById(hallId)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> hallService.getHallById(hallId));

        verify(hallRepository, times(1)).findById(hallId);
    }
}