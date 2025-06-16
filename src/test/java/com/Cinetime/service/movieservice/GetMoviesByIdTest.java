package com.Cinetime.service.movieservice;

import com.Cinetime.entity.Movie;
import com.Cinetime.enums.MovieStatus;
import com.Cinetime.payload.dto.response.MovieResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.mappers.MovieMapper;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.repo.MovieRepository;
import com.Cinetime.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieService - getMoviesById Tests")
class GetMoviesByIdTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieMapper movieMapper;

    @InjectMocks
    private MovieService movieService;

    private Movie testMovie;
    private MovieResponse testMovieResponse;
    private static final Long VALID_MOVIE_ID = 1L;
    private static final Long INVALID_MOVIE_ID = 999L;

    @BeforeEach
    void setUp() {
        // Create test movie entity
        testMovie = Movie.builder()
                .id(VALID_MOVIE_ID)
                .title("Test Movie")
                .slug("test-movie")
                .summary("Test movie summary")
                .releaseDate(LocalDate.of(2024, 1, 15))
                .duration(120)
                .rating(8.5)
                .director("Test Director")
                .cast(Arrays.asList("Actor 1", "Actor 2"))
                .formats(Arrays.asList("IMAX", "Standard"))
                .genre(Arrays.asList("Action", "Adventure"))
                .status(MovieStatus.IN_THEATERS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .posterUrl("https://example.com/poster.jpg")
                .build();

        // Create test movie response
        testMovieResponse = MovieResponse.builder()
                .id(VALID_MOVIE_ID)
                .title("Test Movie")
                .slug("test-movie")
                .summary("Test movie summary")
                .releaseDate(LocalDate.of(2024, 1, 15))
                .duration(120)
                .rating(8.5)
                .director("Test Director")
                .cast(Arrays.asList("Actor 1", "Actor 2"))
                .formats(Arrays.asList("IMAX", "Standard"))
                .genre(Arrays.asList("Action", "Adventure"))
                .status(MovieStatus.IN_THEATERS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .posterUrl("https://example.com/poster.jpg")
                .build();
    }

    @Test
    @DisplayName("Should return movie successfully when movie exists")
    void getMoviesById_WhenMovieExists_ShouldReturnMovieResponse() {
        // Given
        when(movieRepository.findById(VALID_MOVIE_ID)).thenReturn(Optional.of(testMovie));
        when(movieMapper.mapMovieToMovieResponse(testMovie)).thenReturn(testMovieResponse);

        // When
        ResponseMessage<MovieResponse> result = movieService.getMoviesById(VALID_MOVIE_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo("Movies found successfully");
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject()).isEqualTo(testMovieResponse);

        // Verify interactions
        verify(movieRepository, times(1)).findById(VALID_MOVIE_ID);
        verify(movieMapper, times(1)).mapMovieToMovieResponse(testMovie);
        verifyNoMoreInteractions(movieRepository, movieMapper);
    }

    @Test
    @DisplayName("Should return NOT_FOUND when movie does not exist")
    void getMoviesById_WhenMovieDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(movieRepository.findById(INVALID_MOVIE_ID)).thenReturn(Optional.empty());

        // When
        ResponseMessage<MovieResponse> result = movieService.getMoviesById(INVALID_MOVIE_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.MOVIE_NOT_FOUND);
        assertThat(result.getObject()).isNull();

        // Verify interactions
        verify(movieRepository, times(1)).findById(INVALID_MOVIE_ID);
        verifyNoInteractions(movieMapper); // Mapper should not be called when movie is not found
        verifyNoMoreInteractions(movieRepository);
    }

    @Test
    @DisplayName("Should handle null movie ID gracefully")
    void getMoviesById_WhenMovieIdIsNull_ShouldReturnNotFound() {
        // Given
        when(movieRepository.findById(null)).thenReturn(Optional.empty());

        // When
        ResponseMessage<MovieResponse> result = movieService.getMoviesById(null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.MOVIE_NOT_FOUND);
        assertThat(result.getObject()).isNull();

        // Verify interactions
        verify(movieRepository, times(1)).findById(null);
        verifyNoInteractions(movieMapper);
    }

    @Test
    @DisplayName("Should verify mapper is called with correct movie entity")
    void getMoviesById_WhenMovieExists_ShouldCallMapperWithCorrectEntity() {
        // Given
        when(movieRepository.findById(VALID_MOVIE_ID)).thenReturn(Optional.of(testMovie));
        when(movieMapper.mapMovieToMovieResponse(any(Movie.class))).thenReturn(testMovieResponse);

        // When
        movieService.getMoviesById(VALID_MOVIE_ID);

        // Then
        verify(movieMapper).mapMovieToMovieResponse(testMovie);
        // Verify the exact movie object is passed to mapper
        verify(movieMapper).mapMovieToMovieResponse(argThat(movie ->
                movie.getId().equals(VALID_MOVIE_ID) &&
                        movie.getTitle().equals("Test Movie")
        ));
    }

    @Test
    @DisplayName("Should return response with correct generic type")
    void getMoviesById_ShouldReturnCorrectGenericType() {
        // Given
        when(movieRepository.findById(VALID_MOVIE_ID)).thenReturn(Optional.of(testMovie));
        when(movieMapper.mapMovieToMovieResponse(testMovie)).thenReturn(testMovieResponse);

        // When
        ResponseMessage<MovieResponse> result = movieService.getMoviesById(VALID_MOVIE_ID);

        // Then
        // Verify the generic type is correctly maintained
        assertThat(result.getObject()).isInstanceOf(MovieResponse.class);
        assertThat(result.getObject().getId()).isEqualTo(VALID_MOVIE_ID);
        assertThat(result.getObject().getTitle()).isEqualTo("Test Movie");
    }

    @Test
    @DisplayName("Should handle repository returning different movie than expected")
    void getMoviesById_WhenRepositoryReturnsDifferentMovie_ShouldStillMapCorrectly() {
        // Given - Create a different movie with same ID
        Movie differentMovie = Movie.builder()
                .id(VALID_MOVIE_ID)
                .title("Different Movie")
                .slug("different-movie")
                .summary("Different summary")
                .build();

        MovieResponse differentResponse = MovieResponse.builder()
                .id(VALID_MOVIE_ID)
                .title("Different Movie")
                .slug("different-movie")
                .summary("Different summary")
                .build();

        when(movieRepository.findById(VALID_MOVIE_ID)).thenReturn(Optional.of(differentMovie));
        when(movieMapper.mapMovieToMovieResponse(differentMovie)).thenReturn(differentResponse);

        // When
        ResponseMessage<MovieResponse> result = movieService.getMoviesById(VALID_MOVIE_ID);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject().getTitle()).isEqualTo("Different Movie");
        verify(movieMapper).mapMovieToMovieResponse(differentMovie);
    }
}