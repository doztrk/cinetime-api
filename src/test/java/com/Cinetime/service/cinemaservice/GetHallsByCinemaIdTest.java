package com.Cinetime.service.cinemaservice;


import com.Cinetime.entity.Cinema;
import com.Cinetime.entity.Hall;
import com.Cinetime.payload.dto.response.CinemaHallResponse;
import com.Cinetime.payload.dto.response.HallResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.mappers.CinemaHallMapper;
import com.Cinetime.payload.mappers.HallMapper;
import com.Cinetime.repo.CinemaRepository;
import com.Cinetime.service.CinemaService;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetHallsByCinemaIdTest {

    @Mock
    private CinemaRepository cinemaRepository;

    @Mock
    private HallMapper hallMapper;

    @Mock
    private CinemaHallMapper cinemaHallMapper;

    @InjectMocks
    private CinemaService cinemaService;

    @Test
    @DisplayName("Should return cinema halls successfully when cinema exists")
    void getHallsByCinemaId_WhenCinemaExists_ShouldReturnCinemaHalls() {
        // Given
        Long cinemaId = 1L;
        Cinema cinema = createMockCinema();
        List<Hall> halls = createMockHalls();
        List<HallResponse> hallResponses = createMockHallResponses();
        CinemaHallResponse cinemaHallResponse = createMockCinemaHallResponse();

        cinema.setHalls(halls);

        when(cinemaRepository.findById(cinemaId)).thenReturn(Optional.of(cinema));
        when(hallMapper.mapHallToHallResponse(halls)).thenReturn(hallResponses);
        when(cinemaHallMapper.mapToCinemaHallResponse(cinema, hallResponses)).thenReturn(cinemaHallResponse);

        // When
        ResponseMessage<CinemaHallResponse> result = cinemaService.getHallsByCinemaId(cinemaId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Cinema halls found successfully");
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject()).isEqualTo(cinemaHallResponse);

        verify(cinemaRepository).findById(cinemaId);
        verify(hallMapper).mapHallToHallResponse(halls);
        verify(cinemaHallMapper).mapToCinemaHallResponse(cinema, hallResponses);
    }

    @Test
    @DisplayName("Should return NOT_FOUND when cinema does not exist")
    void getHallsByCinemaId_WhenCinemaDoesNotExist_ShouldReturnNotFound() {
        // Given
        Long cinemaId = 999L;

        when(cinemaRepository.findById(cinemaId)).thenReturn(Optional.empty());

        // When
        ResponseMessage<CinemaHallResponse> result = cinemaService.getHallsByCinemaId(cinemaId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Cinema not found");
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getObject()).isNull();

        verify(cinemaRepository).findById(cinemaId);
        verifyNoInteractions(hallMapper);
        verifyNoInteractions(cinemaHallMapper);
    }

    @Test
    @DisplayName("Should handle cinema with null halls list")
    void getHallsByCinemaId_WhenCinemaHasNullHalls_ShouldReturnEmptyList() {
        // Given
        Long cinemaId = 1L;
        Cinema cinema = createMockCinema();
        cinema.setHalls(null); // Explicitly set to null
        List<HallResponse> hallResponses = createMockHallResponses();
        CinemaHallResponse cinemaHallResponse = createMockCinemaHallResponse();

        when(cinemaRepository.findById(cinemaId)).thenReturn(Optional.of(cinema));
        when(hallMapper.mapHallToHallResponse(Collections.emptyList())).thenReturn(hallResponses);
        when(cinemaHallMapper.mapToCinemaHallResponse(cinema, hallResponses)).thenReturn(cinemaHallResponse);

        // When
        ResponseMessage<CinemaHallResponse> result = cinemaService.getHallsByCinemaId(cinemaId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Cinema halls found successfully");
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject()).isEqualTo(cinemaHallResponse);

        verify(cinemaRepository).findById(cinemaId);
        verify(hallMapper).mapHallToHallResponse(Collections.emptyList());
        verify(cinemaHallMapper).mapToCinemaHallResponse(cinema, hallResponses);
    }

    @Test
    @DisplayName("Should handle cinema with empty halls list")
    void getHallsByCinemaId_WhenCinemaHasEmptyHalls_ShouldReturnEmptyList() {
        // Given
        Long cinemaId = 1L;
        Cinema cinema = createMockCinema();
        List<Hall> emptyHalls = Collections.emptyList();
        cinema.setHalls(emptyHalls);
        List<HallResponse> hallResponses = Collections.emptyList();
        CinemaHallResponse cinemaHallResponse = createMockCinemaHallResponse();

        when(cinemaRepository.findById(cinemaId)).thenReturn(Optional.of(cinema));
        when(hallMapper.mapHallToHallResponse(emptyHalls)).thenReturn(hallResponses);
        when(cinemaHallMapper.mapToCinemaHallResponse(cinema, hallResponses)).thenReturn(cinemaHallResponse);

        // When
        ResponseMessage<CinemaHallResponse> result = cinemaService.getHallsByCinemaId(cinemaId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Cinema halls found successfully");
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject()).isEqualTo(cinemaHallResponse);

        verify(cinemaRepository).findById(cinemaId);
        verify(hallMapper).mapHallToHallResponse(emptyHalls);
        verify(cinemaHallMapper).mapToCinemaHallResponse(cinema, hallResponses);
    }

    @Test
    @DisplayName("Should handle null cinemaId parameter")
    void getHallsByCinemaId_WhenCinemaIdIsNull_ShouldCallRepository() {
        // Given
        Long cinemaId = null;

        when(cinemaRepository.findById(cinemaId)).thenReturn(Optional.empty());

        // When
        ResponseMessage<CinemaHallResponse> result = cinemaService.getHallsByCinemaId(cinemaId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Cinema not found");
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(cinemaRepository).findById(cinemaId);
    }

    // Helper methods to create mock objects
    private Cinema createMockCinema() {
        Cinema cinema = new Cinema();
        cinema.setId(1L);
        cinema.setName("Test Cinema");
        return cinema;
    }

    private List<Hall> createMockHalls() {
        Hall hall1 = new Hall();
        hall1.setId(1L);
        hall1.setName("Hall 1");

        Hall hall2 = new Hall();
        hall2.setId(2L);
        hall2.setName("Hall 2");

        return Arrays.asList(hall1, hall2);
    }

    private List<HallResponse> createMockHallResponses() {
        HallResponse response1 = new HallResponse();
        response1.setId(1L);
        response1.setName("Hall 1");

        HallResponse response2 = new HallResponse();
        response2.setId(2L);
        response2.setName("Hall 2");

        return Arrays.asList(response1, response2);
    }

    private CinemaHallResponse createMockCinemaHallResponse() {
        CinemaHallResponse response = new CinemaHallResponse();
        response.setCinemaName("Test Cinema");
        response.setHalls(createMockHallResponses());
        return response;
    }
}