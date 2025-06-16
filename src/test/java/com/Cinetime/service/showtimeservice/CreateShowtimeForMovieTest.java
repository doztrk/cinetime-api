package com.Cinetime.service.showtimeservice;

import com.Cinetime.entity.Cinema;
import com.Cinetime.entity.Hall;
import com.Cinetime.entity.Movie;
import com.Cinetime.entity.Showtime;
import com.Cinetime.helpers.TicketPriceHelper;
import com.Cinetime.payload.dto.request.ShowtimeRequest;
import com.Cinetime.payload.dto.response.*;
import com.Cinetime.payload.mappers.ShowtimeMapper;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.repo.HallRepository;
import com.Cinetime.repo.MovieRepository;
import com.Cinetime.repo.ShowtimeRepository;
import com.Cinetime.service.ShowtimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateShowtimeForMovieTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private HallRepository hallRepository;

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private TicketPriceHelper ticketPriceHelper;

    @Mock
    private ShowtimeMapper showtimeMapper;

    @InjectMocks
    private ShowtimeService showtimeService;

    private ShowtimeRequest showtimeRequest;
    private Movie movie;
    private Hall hall;
    private Cinema cinema;
    private Showtime showtime;
    private ShowtimeResponse showtimeResponse;
    private MovieResponse movieResponse;
    private HallResponse hallResponse;
    private CinemaResponse cinemaResponse;

    @BeforeEach
    void setUp() {
        showtimeRequest = ShowtimeRequest.builder()
                .movieId(1L)
                .hallId(1L)
                .date(LocalDate.of(2024, 12, 25))
                .startTime(LocalTime.of(14, 30))
                .endTime(LocalTime.of(16, 30))
                .build();

        cinema = Cinema.builder()
                .id(1L)
                .name("Test Cinema")
                .address("Test Address")
                .build();

        movie = Movie.builder()
                .id(1L)
                .title("Test Movie")
                .slug("test-movie")
                .summary("Test Summary")
                .releaseDate(LocalDate.of(2024, 12, 1))
                .duration(120)
                .rating(8.5)
                .director("Test Director")
                .build();

        hall = Hall.builder()
                .id(1L)
                .name("Hall 1")
                .seatCapacity(100)
                .isSpecial(false)
                .cinema(cinema)
                .build();

        showtime = Showtime.builder()
                .id(1L)
                .movie(movie)
                .hall(hall)
                .date(showtimeRequest.getDate())
                .startTime(showtimeRequest.getStartTime())
                .endTime(showtimeRequest.getEndTime())
                .price(15.50)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        movieResponse = MovieResponse.builder()
                .id(1L)
                .title("Test Movie")
                .slug("test-movie")
                .summary("Test Summary")
                .releaseDate(LocalDate.of(2024, 12, 1))
                .duration(120)
                .rating(8.5)
                .director("Test Director")
                .build();

        hallResponse = HallResponse.builder()
                .id(1L)
                .name("Hall 1")
                .seatCapacity(100)
                .isSpecial(false)
                .build();

        cinemaResponse = CinemaResponse.builder()
                .id(1L)
                .name("Test Cinema")
                .address("Test Address")
                .build();

        showtimeResponse = ShowtimeResponse.builder()
                .id(1L)
                .date(showtimeRequest.getDate())
                .startTime(showtimeRequest.getStartTime())
                .endTime(showtimeRequest.getEndTime())
                .movie(movieResponse)
                .hall(hallResponse)
                .cinema(cinemaResponse)
                .price(15.50)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create showtime successfully when all data is valid")
    void createShowtime_Success() {
        // Given
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(hallRepository.findById(1L)).thenReturn(Optional.of(hall));
        when(ticketPriceHelper.calculateTicketPrice(hall, movie,
                showtimeRequest.getStartTime(), showtimeRequest.getEndTime(),
                showtimeRequest.getDate())).thenReturn(15.50);
        when(showtimeMapper.mapShowtimeRequestToShowtime(showtimeRequest, movie, hall, 15.50))
                .thenReturn(showtime);
        when(showtimeRepository.save(showtime)).thenReturn(showtime);
        when(showtimeMapper.mapShowtimeToShowtimeResponse(showtime)).thenReturn(showtimeResponse);

        // When
        ResponseMessage<ShowtimeResponse> result = showtimeService.createShowtimeForMovie(showtimeRequest);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK); // Should be CREATED
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.SHOWTIME_CREATED_SUCCESSFULLY);
        assertThat(result.getObject()).isEqualTo(showtimeResponse);
        assertThat(result.getObject().getMovie().getTitle()).isEqualTo("Test Movie");
        assertThat(result.getObject().getHall().getName()).isEqualTo("Hall 1");
        assertThat(result.getObject().getCinema().getName()).isEqualTo("Test Cinema");

        verify(movieRepository).findById(1L);
        verify(hallRepository).findById(1L);
        verify(ticketPriceHelper).calculateTicketPrice(hall, movie,
                showtimeRequest.getStartTime(), showtimeRequest.getEndTime(),
                showtimeRequest.getDate());
        verify(showtimeMapper).mapShowtimeRequestToShowtime(showtimeRequest, movie, hall, 15.50);
        verify(showtimeRepository).save(showtime);
        verify(showtimeMapper).mapShowtimeToShowtimeResponse(showtime);
    }

    @Test
    @DisplayName("Should return NOT_FOUND when movie does not exist")
    void createShowtime_MovieNotFound() {
        // Given
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        ResponseMessage<ShowtimeResponse> result = showtimeService.createShowtimeForMovie(showtimeRequest);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.MOVIE_NOT_FOUND);
        assertThat(result.getObject()).isNull();

        verify(movieRepository).findById(1L);
        verify(hallRepository, never()).findById(anyLong());
        verify(showtimeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return NOT_FOUND when hall does not exist")
    void createShowtime_HallNotFound() {
        // Given
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(hallRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        ResponseMessage<ShowtimeResponse> result = showtimeService.createShowtimeForMovie(showtimeRequest);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.HALL_NOT_FOUND);
        assertThat(result.getObject()).isNull();

        verify(movieRepository).findById(1L);
        verify(hallRepository).findById(1L);
        verify(showtimeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle null showtime request")
    void createShowtime_NullRequest() {
        // When & Then
        assertThatThrownBy(() -> showtimeService.createShowtimeForMovie(null))
                .isInstanceOf(NullPointerException.class);

        verify(movieRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Should handle repository save failure")
    void createShowtime_SaveFailure() {
        // Given
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(hallRepository.findById(1L)).thenReturn(Optional.of(hall));
        when(ticketPriceHelper.calculateTicketPrice(any(), any(), any(), any(), any()))
                .thenReturn(15.50);
        when(showtimeMapper.mapShowtimeRequestToShowtime(any(), any(), any(), any()))
                .thenReturn(showtime);
        when(showtimeRepository.save(showtime))
                .thenThrow(new DataAccessException("Database error") {
                });

        // When & Then
        assertThatThrownBy(() -> showtimeService.createShowtimeForMovie(showtimeRequest))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("Database error");
    }

    @Test
    @DisplayName("Should handle price calculation failure")
    void createShowtime_PriceCalculationFailure() {
        // Given
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(hallRepository.findById(1L)).thenReturn(Optional.of(hall));
        when(ticketPriceHelper.calculateTicketPrice(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Price calculation failed"));

        // When & Then
        assertThatThrownBy(() -> showtimeService.createShowtimeForMovie(showtimeRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Price calculation failed");
    }
}