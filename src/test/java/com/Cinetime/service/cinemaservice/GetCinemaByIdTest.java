package com.Cinetime.service.cinemaservice;

import com.Cinetime.entity.Cinema;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.repo.CinemaRepository;
import com.Cinetime.service.CinemaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CinemaService - getCinemaById Tests")
class GetCinemaByIdTest {

    @Mock
    private CinemaRepository cinemaRepository;

    @InjectMocks
    private CinemaService cinemaService;

    private Cinema testCinema;
    private static final Long VALID_CINEMA_ID = 1L;
    private static final Long INVALID_CINEMA_ID = 999L;

    @BeforeEach
    void setUp() {
        testCinema = Cinema.builder()
                .id(VALID_CINEMA_ID)
                .name("Test Cinema")
                .slug("test-cinema")
                .address("Test Address")
                .phone("123-456-7890")
                .email("test@cinema.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should return cinema successfully when cinema exists")
    void getCinemaById_WhenCinemaExists_ShouldReturnCinemaSuccessfully() {
        // Given
        when(cinemaRepository.findById(VALID_CINEMA_ID)).thenReturn(Optional.of(testCinema));

        // When
        ResponseMessage<Cinema> result = cinemaService.getCinemaById(VALID_CINEMA_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo("Cinema found");
        assertThat(result.getObject()).isEqualTo(testCinema);
        assertThat(result.getObject().getId()).isEqualTo(VALID_CINEMA_ID);
        assertThat(result.getObject().getName()).isEqualTo("Test Cinema");

        verify(cinemaRepository).findById(VALID_CINEMA_ID);
    }

    @Test
    @DisplayName("Should return NOT_FOUND when cinema does not exist")
    void getCinemaById_WhenCinemaDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(cinemaRepository.findById(INVALID_CINEMA_ID)).thenReturn(Optional.empty());

        // When
        ResponseMessage<Cinema> result = cinemaService.getCinemaById(INVALID_CINEMA_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessage()).isEqualTo("Cinema not found");
        assertThat(result.getObject()).isNull();

        verify(cinemaRepository).findById(INVALID_CINEMA_ID);
    }

    @Test
    @DisplayName("Should handle null ID gracefully")
    void getCinemaById_WhenIdIsNull_ShouldReturnNotFound() {
        // Given
        Long nullId = null;
        when(cinemaRepository.findById(nullId)).thenReturn(Optional.empty());

        // When
        ResponseMessage<Cinema> result = cinemaService.getCinemaById(nullId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessage()).isEqualTo("Cinema not found");
        assertThat(result.getObject()).isNull();

        verify(cinemaRepository).findById(nullId);
    }

    @Test
    @DisplayName("Should verify repository interaction occurs exactly once")
    void getCinemaById_ShouldCallRepositoryOnlyOnce() {
        // Given
        when(cinemaRepository.findById(VALID_CINEMA_ID)).thenReturn(Optional.of(testCinema));

        // When
        cinemaService.getCinemaById(VALID_CINEMA_ID);

        // Then
        verify(cinemaRepository).findById(VALID_CINEMA_ID);
    }
}