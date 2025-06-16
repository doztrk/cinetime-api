package com.Cinetime.service.cinemaservice;


import com.Cinetime.entity.Cinema;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.payload.dto.response.CinemaResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.mappers.CinemaMapper;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.repo.CinemaRepository;
import com.Cinetime.service.CinemaService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCinemasByMovieIdTest {

    @Mock
    private CinemaRepository cinemaRepository;

    @Mock
    private PageableHelper pageableHelper;

    @Mock
    private CinemaMapper cinemaMapper;

    @InjectMocks
    private CinemaService cinemaService;

    private Cinema testCinema1;
    private Cinema testCinema2;
    private CinemaResponse testCinemaResponse1;
    private CinemaResponse testCinemaResponse2;
    private Pageable testPageable;
    private Long testMovieId;

    @BeforeEach
    void setUp() {
        testMovieId = 1L;
        testPageable = PageRequest.of(0, 10);

        // Test Cinema entities
        testCinema1 = Cinema.builder()
                .id(1L)
                .name("Cinema City")
                .address("Downtown")
                .build();

        testCinema2 = Cinema.builder()
                .id(2L)
                .name("Grand Cinema")
                .address("Mall Area")
                .build();

        // Test CinemaResponse DTOs
        testCinemaResponse1 = CinemaResponse.builder()
                .id(1L)
                .name("Cinema City")
                .address("Downtown")
                .build();

        testCinemaResponse2 = CinemaResponse.builder()
                .id(2L)
                .name("Grand Cinema")
                .address("Mall Area")
                .build();
    }

    @Test
    @DisplayName("Should return cinemas successfully when cinemas exist for movie")
    void getCinemasByMovieId_WithExistingCinemas_ShouldReturnSuccessResponse() {
        // Given
        int page = 0, size = 10;
        String sort = "name", type = "asc";

        List<Cinema> cinemaList = Arrays.asList(testCinema1, testCinema2);
        Page<Cinema> cinemaPage = new PageImpl<>(cinemaList, testPageable, 2);
        Page<CinemaResponse> cinemaResponsePage = new PageImpl<>(
                Arrays.asList(testCinemaResponse1, testCinemaResponse2),
                testPageable,
                2
        );

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(cinemaRepository.findCinemasByMovieId(testMovieId, testPageable)).thenReturn(cinemaPage);
        when(cinemaMapper.mapCinemaToCinemaResponse(testCinema1)).thenReturn(testCinemaResponse1);
        when(cinemaMapper.mapCinemaToCinemaResponse(testCinema2)).thenReturn(testCinemaResponse2);

        // When
        ResponseMessage<Page<CinemaResponse>> result = cinemaService.getCinemasByMovieId(
                testMovieId, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.CINEMA_FOUND);
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getContent()).hasSize(2);
        assertThat(result.getObject().getTotalElements()).isEqualTo(2);

        // Verify interactions
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(cinemaRepository).findCinemasByMovieId(testMovieId, testPageable);
        verify(cinemaMapper).mapCinemaToCinemaResponse(testCinema1);
        verify(cinemaMapper).mapCinemaToCinemaResponse(testCinema2);
    }

    @Test
    @DisplayName("Should return NO_CONTENT when no cinemas exist for movie")
    void getCinemasByMovieId_WithNoCinemas_ShouldReturnNoContentResponse() {
        // Given
        int page = 0, size = 10;
        String sort = "name", type = "asc";

        Page<Cinema> emptyCinemaPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(cinemaRepository.findCinemasByMovieId(testMovieId, testPageable)).thenReturn(emptyCinemaPage);

        // When
        ResponseMessage<Page<CinemaResponse>> result = cinemaService.getCinemasByMovieId(
                testMovieId, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.CINEMA_NOT_FOUND);
        assertThat(result.getObject()).isNull();

        // Verify interactions
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(cinemaRepository).findCinemasByMovieId(testMovieId, testPageable);
        verify(cinemaMapper, never()).mapCinemaToCinemaResponse(any());
    }

    @Test
    @DisplayName("Should handle different pagination parameters correctly")
    void getCinemasByMovieId_WithCustomPagination_ShouldPassCorrectParameters() {
        // Given
        int page = 2, size = 5;
        String sort = "location", type = "desc";
        Pageable customPageable = PageRequest.of(page, size);

        List<Cinema> singleCinemaList = Collections.singletonList(testCinema1);
        Page<Cinema> singleCinemaPage = new PageImpl<>(singleCinemaList, customPageable, 1);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(customPageable);
        when(cinemaRepository.findCinemasByMovieId(testMovieId, customPageable)).thenReturn(singleCinemaPage);
        when(cinemaMapper.mapCinemaToCinemaResponse(testCinema1)).thenReturn(testCinemaResponse1);

        // When
        cinemaService.getCinemasByMovieId(testMovieId, page, size, sort, type);

        // Then - Verify exact parameters were passed
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(cinemaRepository).findCinemasByMovieId(testMovieId, customPageable);
    }

    @Test
    @DisplayName("Should handle single cinema result correctly")
    void getCinemasByMovieId_WithSingleCinema_ShouldReturnSingleCinemaResponse() {
        // Given
        int page = 0, size = 10;
        String sort = "name", type = "asc";

        List<Cinema> singleCinemaList = Collections.singletonList(testCinema1);
        Page<Cinema> singleCinemaPage = new PageImpl<>(singleCinemaList, testPageable, 1);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(cinemaRepository.findCinemasByMovieId(testMovieId, testPageable)).thenReturn(singleCinemaPage);
        when(cinemaMapper.mapCinemaToCinemaResponse(testCinema1)).thenReturn(testCinemaResponse1);

        // When
        ResponseMessage<Page<CinemaResponse>> result = cinemaService.getCinemasByMovieId(
                testMovieId, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.CINEMA_FOUND);
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getContent()).hasSize(1);
        assertThat(result.getObject().getContent().get(0).getName()).isEqualTo("Cinema City");
        assertThat(result.getObject().getTotalElements()).isEqualTo(1);

        // Verify interactions
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(cinemaRepository).findCinemasByMovieId(testMovieId, testPageable);
        verify(cinemaMapper).mapCinemaToCinemaResponse(testCinema1);
    }

    @Test
    @DisplayName("Should handle different movie IDs correctly")
    void getCinemasByMovieId_WithDifferentMovieIds_ShouldCallRepositoryWithCorrectId() {
        // Given
        Long differentMovieId = 999L;
        int page = 0, size = 10;
        String sort = "name", type = "asc";

        Page<Cinema> emptyCinemaPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(cinemaRepository.findCinemasByMovieId(differentMovieId, testPageable)).thenReturn(emptyCinemaPage);

        // When
        cinemaService.getCinemasByMovieId(differentMovieId, page, size, sort, type);

        // Then - Verify correct movie ID was passed
        verify(cinemaRepository).findCinemasByMovieId(differentMovieId, testPageable);
        verify(cinemaRepository, never()).findCinemasByMovieId(testMovieId, testPageable);
    }
}