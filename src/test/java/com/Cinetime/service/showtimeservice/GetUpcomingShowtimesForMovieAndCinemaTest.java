package com.Cinetime.service.showtimeservice;

import com.Cinetime.entity.Movie;
import com.Cinetime.entity.Showtime;
import com.Cinetime.entity.Hall;
import com.Cinetime.entity.Cinema;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.dto.response.ShowtimeResponse;
import com.Cinetime.payload.dto.response.MovieResponse;
import com.Cinetime.payload.dto.response.HallResponse;
import com.Cinetime.payload.dto.response.CinemaResponse;
import com.Cinetime.payload.mappers.ShowtimeMapper;
import com.Cinetime.repo.ShowtimeRepository;
import com.Cinetime.service.ShowtimeService;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShowtimeService - getUpcomingShowtimesForMovieAndCinema Tests")
class GetUpcomingShowtimesForMovieAndCinemaTest {

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private PageableHelper pageableHelper;

    @Mock
    private ShowtimeMapper showtimeMapper;

    @InjectMocks
    private ShowtimeService showtimeService;

    private Showtime testShowtime1;
    private Showtime testShowtime2;
    private ShowtimeResponse showtimeResponse1;
    private ShowtimeResponse showtimeResponse2;
    private Page<ShowtimeResponse> mockShowtimeResponsePage;
    private Pageable testPageable;
    private Movie testMovie;
    private Hall testHall;
    private Cinema testCinema;

    private static final Long VALID_MOVIE_ID = 1L;
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final String DEFAULT_SORT = "startTime";
    private static final String DEFAULT_TYPE = "asc";

    @BeforeEach
    void setUp() {
        // Setup test entities
        testCinema = Cinema.builder()
                .id(1L)
                .name("Test Cinema")
                .slug("test-cinema")
                .build();

        testHall = Hall.builder()
                .id(1L)
                .name("Hall A")
                .cinema(testCinema)
                .build();

        testMovie = Movie.builder()
                .id(VALID_MOVIE_ID)
                .title("Test Movie")
                .build();

        testShowtime1 = Showtime.builder()
                .id(1L)
                .movie(testMovie)
                .hall(testHall)
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(19, 0))
                .endTime(LocalTime.of(21, 30))
                .build();

        testShowtime2 = Showtime.builder()
                .id(2L)
                .movie(testMovie)
                .hall(testHall)
                .date(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(20, 0))
                .endTime(LocalTime.of(22, 30))
                .build();

        // Create nested response objects
        MovieResponse movieResponse = MovieResponse.builder()
                .id(VALID_MOVIE_ID)
                .title("Test Movie")
                .slug("test-movie")
                .summary("Test Summary")
                .releaseDate(LocalDate.now().minusMonths(1))
                .duration(120)
                .rating(8.5)
                .director("Test Director")
                .build();

        HallResponse hallResponse = HallResponse.builder()
                .id(1L)
                .name("Hall A")
                .seatCapacity(100)
                .build();

        CinemaResponse cinemaResponse = CinemaResponse.builder()
                .id(1L)
                .name("Test Cinema")
                .slug("test-cinema")
                .build();

        showtimeResponse1 = ShowtimeResponse.builder()
                .id(1L)
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(19, 0))
                .endTime(LocalTime.of(21, 30))
                .movie(movieResponse)
                .hall(hallResponse)
                .cinema(cinemaResponse)
                .price(15.50)
                .build();

        showtimeResponse2 = ShowtimeResponse.builder()
                .id(2L)
                .date(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(20, 0))
                .endTime(LocalTime.of(22, 30))
                .movie(movieResponse)
                .hall(hallResponse)
                .cinema(cinemaResponse)
                .price(15.50)
                .build();

        testPageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE, Sort.by(DEFAULT_SORT).ascending());
        mockShowtimeResponsePage = new PageImpl<>(
                Arrays.asList(showtimeResponse1, showtimeResponse2),
                testPageable,
                2
        );
    }

    @Test
    @DisplayName("Should return upcoming showtimes successfully when found")
    void getUpcomingShowtimesForMovieAndCinema_WhenShowtimesExist_ShouldReturnSuccessfully() {
        // Given
        List<Showtime> showtimeList = Arrays.asList(testShowtime1, testShowtime2);
        Page<Showtime> showtimePage = new PageImpl<>(showtimeList, testPageable, 2);

        when(pageableHelper.pageableSort(DEFAULT_PAGE, DEFAULT_SIZE, DEFAULT_SORT, DEFAULT_TYPE))
                .thenReturn(testPageable);
        when(showtimeRepository.findUpcomingShowtimesByMovieId(
                eq(VALID_MOVIE_ID),
                any(LocalDate.class),
                any(LocalTime.class),
                eq(testPageable)))
                .thenReturn(showtimePage);
        when(showtimeMapper.mapShowtimePageToShowtimeResponse(showtimePage))
                .thenReturn(mockShowtimeResponsePage);

        // When
        ResponseMessage<Page<ShowtimeResponse>> result = showtimeService
                .getUpcomingShowtimesForMovieAndCinema(DEFAULT_PAGE, DEFAULT_SIZE, DEFAULT_SORT, DEFAULT_TYPE, VALID_MOVIE_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo("Showtimes found successfully");
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getContent()).hasSize(2);
        assertThat(result.getObject().getTotalElements()).isEqualTo(2);

        // Verify method call order
        var inOrder = inOrder(pageableHelper, showtimeRepository, showtimeMapper);
        inOrder.verify(pageableHelper).pageableSort(DEFAULT_PAGE, DEFAULT_SIZE, DEFAULT_SORT, DEFAULT_TYPE);
        inOrder.verify(showtimeRepository).findUpcomingShowtimesByMovieId(
                eq(VALID_MOVIE_ID), any(LocalDate.class), any(LocalTime.class), eq(testPageable));
        inOrder.verify(showtimeMapper).mapShowtimePageToShowtimeResponse(showtimePage);
    }

    @Test
    @DisplayName("Should return NO_CONTENT when no showtimes found")
    void getUpcomingShowtimesForMovieAndCinema_WhenNoShowtimesFound_ShouldReturnNoContent() {
        // Given
        Page<Showtime> emptyPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);

        when(pageableHelper.pageableSort(DEFAULT_PAGE, DEFAULT_SIZE, DEFAULT_SORT, DEFAULT_TYPE))
                .thenReturn(testPageable);
        when(showtimeRepository.findUpcomingShowtimesByMovieId(
                eq(VALID_MOVIE_ID),
                any(LocalDate.class),
                any(LocalTime.class),
                eq(testPageable)))
                .thenReturn(emptyPage);

        // When
        ResponseMessage<Page<ShowtimeResponse>> result = showtimeService
                .getUpcomingShowtimesForMovieAndCinema(DEFAULT_PAGE, DEFAULT_SIZE, DEFAULT_SORT, DEFAULT_TYPE, VALID_MOVIE_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getMessage()).isEqualTo("Showtimes not found for the given movie");
        assertThat(result.getObject()).isNull();

        // Verify mapper is not called when no showtimes found
        verify(showtimeMapper, never()).mapShowtimePageToShowtimeResponse(any());
    }

    @Test
    @DisplayName("Should handle different pagination parameters correctly")
    void getUpcomingShowtimesForMovieAndCinema_WithDifferentPaginationParams_ShouldHandleCorrectly() {
        // Given
        int customPage = 2, customSize = 5;
        String customSort = "date", customType = "desc";
        Pageable customPageable = PageRequest.of(customPage, customSize, Sort.by(customSort).descending());

        List<Showtime> showtimeList = Arrays.asList(testShowtime1);
        Page<Showtime> showtimePage = new PageImpl<>(showtimeList, customPageable, 1);
        Page<ShowtimeResponse> responsePageSingle = new PageImpl<>(
                Arrays.asList(showtimeResponse1), customPageable, 1);

        when(pageableHelper.pageableSort(customPage, customSize, customSort, customType))
                .thenReturn(customPageable);
        when(showtimeRepository.findUpcomingShowtimesByMovieId(
                eq(VALID_MOVIE_ID),
                any(LocalDate.class),
                any(LocalTime.class),
                eq(customPageable)))
                .thenReturn(showtimePage);
        when(showtimeMapper.mapShowtimePageToShowtimeResponse(showtimePage))
                .thenReturn(responsePageSingle);

        // When
        ResponseMessage<Page<ShowtimeResponse>> result = showtimeService
                .getUpcomingShowtimesForMovieAndCinema(customPage, customSize, customSort, customType, VALID_MOVIE_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getNumber()).isEqualTo(customPage);
        assertThat(result.getObject().getSize()).isEqualTo(customSize);

        verify(pageableHelper).pageableSort(eq(customPage), eq(customSize), eq(customSort), eq(customType));
    }

    @Test
    @DisplayName("Should pass current date and time to repository correctly")
    void getUpcomingShowtimesForMovieAndCinema_ShouldPassCurrentDateTimeToRepository() {
        // Given
        Page<Showtime> showtimePage = new PageImpl<>(Arrays.asList(testShowtime1), testPageable, 1);

        when(pageableHelper.pageableSort(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(testPageable);
        when(showtimeRepository.findUpcomingShowtimesByMovieId(
                eq(VALID_MOVIE_ID),
                any(LocalDate.class),
                any(LocalTime.class),
                eq(testPageable)))
                .thenReturn(showtimePage);
        when(showtimeMapper.mapShowtimePageToShowtimeResponse(any()))
                .thenReturn(mockShowtimeResponsePage);

        // When
        showtimeService.getUpcomingShowtimesForMovieAndCinema(
                DEFAULT_PAGE, DEFAULT_SIZE, DEFAULT_SORT, DEFAULT_TYPE, VALID_MOVIE_ID);

        // Then - Verify that current date and time are passed to repository
        verify(showtimeRepository).findUpcomingShowtimesByMovieId(
                eq(VALID_MOVIE_ID),
                argThat(date -> date.equals(LocalDate.now())),
                argThat(time -> time.getHour() >= 0 && time.getHour() <= 23), // Time should be valid current time
                eq(testPageable)
        );
    }

    @Test
    @DisplayName("Should handle null movie ID gracefully")
    void getUpcomingShowtimesForMovieAndCinema_WithNullMovieId_ShouldHandleGracefully() {
        // Given
        Page<Showtime> emptyPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);

        when(pageableHelper.pageableSort(DEFAULT_PAGE, DEFAULT_SIZE, DEFAULT_SORT, DEFAULT_TYPE))
                .thenReturn(testPageable);
        when(showtimeRepository.findUpcomingShowtimesByMovieId(
                eq(null),
                any(LocalDate.class),
                any(LocalTime.class),
                eq(testPageable)))
                .thenReturn(emptyPage);

        // When
        ResponseMessage<Page<ShowtimeResponse>> result = showtimeService
                .getUpcomingShowtimesForMovieAndCinema(DEFAULT_PAGE, DEFAULT_SIZE, DEFAULT_SORT, DEFAULT_TYPE, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getMessage()).isEqualTo("Showtimes not found for the given movie");
        assertThat(result.getObject()).isNull();

        verify(showtimeRepository).findUpcomingShowtimesByMovieId(
                eq(null), any(LocalDate.class), any(LocalTime.class), eq(testPageable));
    }

    @Test
    @DisplayName("Should maintain response message structure consistency")
    void getUpcomingShowtimesForMovieAndCinema_ShouldMaintainResponseStructureConsistency() {
        // Given
        Page<Showtime> showtimePage = new PageImpl<>(Arrays.asList(testShowtime1), testPageable, 1);

        when(pageableHelper.pageableSort(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(testPageable);
        when(showtimeRepository.findUpcomingShowtimesByMovieId(any(), any(), any(), any()))
                .thenReturn(showtimePage);
        when(showtimeMapper.mapShowtimePageToShowtimeResponse(any()))
                .thenReturn(mockShowtimeResponsePage);

        // When
        ResponseMessage<Page<ShowtimeResponse>> result = showtimeService
                .getUpcomingShowtimesForMovieAndCinema(DEFAULT_PAGE, DEFAULT_SIZE, DEFAULT_SORT, DEFAULT_TYPE, VALID_MOVIE_ID);

        // Then - Verify ResponseMessage structure
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isNotNull().isNotEmpty();
        assertThat(result.getHttpStatus()).isNotNull();
        assertThat(result.getObject()).isNotNull();

        // Verify Page structure
        Page<ShowtimeResponse> responsePage = result.getObject();
        assertThat(responsePage.getContent()).isNotNull();
        assertThat(responsePage.getTotalElements()).isNotNegative();
        assertThat(responsePage.getNumber()).isNotNegative();
        assertThat(responsePage.getSize()).isPositive();
    }

    @Test
    @DisplayName("Should verify exact parameter passing to pageableHelper")
    void getUpcomingShowtimesForMovieAndCinema_ShouldPassExactParametersToPageableHelper() {
        // Given
        int page = 3, size = 15;
        String sort = "endTime", type = "desc";
        Page<Showtime> emptyPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(showtimeRepository.findUpcomingShowtimesByMovieId(any(), any(), any(), any()))
                .thenReturn(emptyPage);

        // When
        showtimeService.getUpcomingShowtimesForMovieAndCinema(page, size, sort, type, VALID_MOVIE_ID);

        // Then
        verify(pageableHelper).pageableSort(eq(page), eq(size), eq(sort), eq(type));
    }

    @Test
    @DisplayName("Should verify mapper is called with exact showtime page")
    void getUpcomingShowtimesForMovieAndCinema_WhenShowtimesExist_ShouldCallMapperWithExactPage() {
        // Given
        Page<Showtime> showtimePage = new PageImpl<>(Arrays.asList(testShowtime1, testShowtime2), testPageable, 2);

        when(pageableHelper.pageableSort(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(testPageable);
        when(showtimeRepository.findUpcomingShowtimesByMovieId(any(), any(), any(), any()))
                .thenReturn(showtimePage);
        when(showtimeMapper.mapShowtimePageToShowtimeResponse(showtimePage))
                .thenReturn(mockShowtimeResponsePage);

        // When
        showtimeService.getUpcomingShowtimesForMovieAndCinema(
                DEFAULT_PAGE, DEFAULT_SIZE, DEFAULT_SORT, DEFAULT_TYPE, VALID_MOVIE_ID);

        // Then
        verify(showtimeMapper).mapShowtimePageToShowtimeResponse(showtimePage);
        verify(showtimeMapper).mapShowtimePageToShowtimeResponse(argThat(page ->
                page.getContent().size() == 2 &&
                        page.getTotalElements() == 2
        ));
    }
}