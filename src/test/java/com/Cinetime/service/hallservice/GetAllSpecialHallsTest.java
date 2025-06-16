package com.Cinetime.service.hallservice;

import com.Cinetime.entity.Hall;
import com.Cinetime.payload.dto.response.HallResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.mappers.HallMapper;
import com.Cinetime.repo.HallRepository;
import com.Cinetime.service.HallService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAllSpecialHallsTest {

    @Mock
    private HallRepository hallRepository;

    @Mock
    private HallMapper hallMapper;

    @InjectMocks
    private HallService hallService;

    private Hall mockSpecialHall1;
    private Hall mockSpecialHall2;
    private HallResponse mockHallResponse1;
    private HallResponse mockHallResponse2;
    private List<Hall> mockSpecialHalls;
    private List<HallResponse> mockHallResponses;

    @BeforeEach
    void setUp() {
        // Create mock Hall entities
        mockSpecialHall1 = Hall.builder()
                .id(1L)
                .name("VIP Hall 1")
                .seatCapacity(50)
                .isSpecial(true)
                .build();

        mockSpecialHall2 = Hall.builder()
                .id(2L)
                .name("IMAX Hall")
                .seatCapacity(100)
                .isSpecial(true)
                .build();

        // Create mock HallResponse DTOs
        mockHallResponse1 = HallResponse.builder()
                .id(1L)
                .name("VIP Hall 1")
                .seatCapacity(50)
                .isSpecial(true)
                .build();

        mockHallResponse2 = HallResponse.builder()
                .id(2L)
                .name("IMAX Hall")
                .seatCapacity(100)
                .isSpecial(true)
                .build();

        // Create lists
        mockSpecialHalls = List.of(mockSpecialHall1, mockSpecialHall2);
        mockHallResponses = List.of(mockHallResponse1, mockHallResponse2);
    }

    @Test
    @DisplayName("Should return all special halls successfully")
    void getAllSpecialHalls_WithSpecialHallsExist_ShouldReturnSuccessResponse() {
        // Given
        when(hallRepository.findByIsSpecialTrue()).thenReturn(mockSpecialHalls);
        when(hallMapper.mapHallToHallResponse(mockSpecialHalls)).thenReturn(mockHallResponses);

        // When
        ResponseMessage<List<HallResponse>> result = hallService.getAllSpecialHalls();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject()).hasSize(2);
        assertThat(result.getObject()).containsExactlyInAnyOrder(mockHallResponse1, mockHallResponse2);

        // Verify interactions
        verify(hallRepository, times(1)).findByIsSpecialTrue();
        verify(hallMapper, times(1)).mapHallToHallResponse(mockSpecialHalls);
        verifyNoMoreInteractions(hallRepository, hallMapper);
    }

    @Test
    @DisplayName("Should return empty list when no special halls exist")
    void getAllSpecialHalls_WithNoSpecialHalls_ShouldReturnEmptyList() {
        // Given
        List<Hall> emptyHallList = Collections.emptyList();
        List<HallResponse> emptyResponseList = Collections.emptyList();

        when(hallRepository.findByIsSpecialTrue()).thenReturn(emptyHallList);
        when(hallMapper.mapHallToHallResponse(emptyHallList)).thenReturn(emptyResponseList);

        // When
        ResponseMessage<List<HallResponse>> result = hallService.getAllSpecialHalls();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject()).isEmpty();

        // Verify interactions
        verify(hallRepository, times(1)).findByIsSpecialTrue();
        verify(hallMapper, times(1)).mapHallToHallResponse(emptyHallList);
        verifyNoMoreInteractions(hallRepository, hallMapper);
    }

    @Test
    @DisplayName("Should return single special hall when only one exists")
    void getAllSpecialHalls_WithSingleSpecialHall_ShouldReturnSingleItem() {
        // Given
        List<Hall> singleHallList = List.of(mockSpecialHall1);
        List<HallResponse> singleResponseList = List.of(mockHallResponse1);

        when(hallRepository.findByIsSpecialTrue()).thenReturn(singleHallList);
        when(hallMapper.mapHallToHallResponse(singleHallList)).thenReturn(singleResponseList);

        // When
        ResponseMessage<List<HallResponse>> result = hallService.getAllSpecialHalls();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject()).hasSize(1);
        assertThat(result.getObject()).containsExactly(mockHallResponse1);

        // Verify interactions
        verify(hallRepository, times(1)).findByIsSpecialTrue();
        verify(hallMapper, times(1)).mapHallToHallResponse(singleHallList);
        verifyNoMoreInteractions(hallRepository, hallMapper);
    }

    @Test
    @DisplayName("Should handle repository exception gracefully")
    void getAllSpecialHalls_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Given
        when(hallRepository.findByIsSpecialTrue()).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThat(
                org.junit.jupiter.api.Assertions.assertThrows(
                        RuntimeException.class,
                        () -> hallService.getAllSpecialHalls()
                )
        ).hasMessage("Database connection failed");

        // Verify interactions
        verify(hallRepository, times(1)).findByIsSpecialTrue();
        verifyNoInteractions(hallMapper);
    }

    @Test
    @DisplayName("Should handle mapper exception gracefully")
    void getAllSpecialHalls_WhenMapperThrowsException_ShouldPropagateException() {
        // Given
        when(hallRepository.findByIsSpecialTrue()).thenReturn(mockSpecialHalls);
        when(hallMapper.mapHallToHallResponse(mockSpecialHalls))
                .thenThrow(new RuntimeException("Mapping failed"));

        // When & Then
        assertThat(
                org.junit.jupiter.api.Assertions.assertThrows(
                        RuntimeException.class,
                        () -> hallService.getAllSpecialHalls()
                )
        ).hasMessage("Mapping failed");

        // Verify interactions
        verify(hallRepository, times(1)).findByIsSpecialTrue();
        verify(hallMapper, times(1)).mapHallToHallResponse(mockSpecialHalls);
        verifyNoMoreInteractions(hallRepository, hallMapper);
    }

    @Test
    @DisplayName("Should verify correct method call sequence")
    void getAllSpecialHalls_ShouldCallMethodsInCorrectOrder() {
        // Given
        when(hallRepository.findByIsSpecialTrue()).thenReturn(mockSpecialHalls);
        when(hallMapper.mapHallToHallResponse(mockSpecialHalls)).thenReturn(mockHallResponses);

        // When
        hallService.getAllSpecialHalls();

        // Then - verify call order using InOrder
        var inOrder = inOrder(hallRepository, hallMapper);
        inOrder.verify(hallRepository).findByIsSpecialTrue();
        inOrder.verify(hallMapper).mapHallToHallResponse(mockSpecialHalls);
        inOrder.verifyNoMoreInteractions();
    }
}