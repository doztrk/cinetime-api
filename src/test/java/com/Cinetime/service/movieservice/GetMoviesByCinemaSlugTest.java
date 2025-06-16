package com.Cinetime.service.movieservice;

import com.Cinetime.payload.dto.response.MovieResponseCinema;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.repo.MovieRepository;
import com.Cinetime.service.MovieService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetMoviesByCinemaSlugTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    @Test
    @DisplayName("Should return movies successfully when cinema slug exists")
    void getMoviesByCinemaSlug_WhenMoviesExist_ShouldReturnMoviesSuccessfully() {
        // Given
        String cinemaSlug = "marmara-park";
        int page = 0;
        int size = 10;
        String sort = "name";
        String type = "desc";

        List<MovieResponseCinema> expectedMovies = Arrays.asList(
                createMockMovieResponseCinema(1L, "Movie 1"),
                createMockMovieResponseCinema(2L, "Movie 2")
        );

        when(movieRepository.findMoviesByCinemaSlug(cinemaSlug)).thenReturn(expectedMovies);

        // When
        ResponseMessage<List<MovieResponseCinema>> result = movieService.getMoviesByCinemaSlug(
                cinemaSlug, page, size, sort, type
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo("Movies found successfully");
        assertThat(result.getObject()).isEqualTo(expectedMovies);
        assertThat(result.getObject()).hasSize(2);

        verify(movieRepository).findMoviesByCinemaSlug(cinemaSlug);
    }

    @Test
    @DisplayName("Should return NOT_FOUND when no movies exist for cinema slug")
    void getMoviesByCinemaSlug_WhenNoMoviesExist_ShouldReturnNotFound() {
        // Given
        String cinemaSlug = "non-existent-cinema";
        int page = 0;
        int size = 10;
        String sort = "name";
        String type = "asc";

        List<MovieResponseCinema> emptyMovieList = Collections.emptyList();

        when(movieRepository.findMoviesByCinemaSlug(cinemaSlug)).thenReturn(emptyMovieList);

        // When
        ResponseMessage<List<MovieResponseCinema>> result = movieService.getMoviesByCinemaSlug(
                cinemaSlug, page, size, sort, type
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.MOVIE_NOT_FOUND);
        assertThat(result.getObject()).isNull();

        verify(movieRepository).findMoviesByCinemaSlug(cinemaSlug);
    }

    @Test
    @DisplayName("Should handle null cinema slug gracefully")
    void getMoviesByCinemaSlug_WhenCinemaSlugIsNull_ShouldReturnNotFound() {
        // Given
        String cinemaSlug = null;
        int page = 0;
        int size = 10;
        String sort = "name";
        String type = "asc";

        List<MovieResponseCinema> emptyMovieList = Collections.emptyList();

        when(movieRepository.findMoviesByCinemaSlug(cinemaSlug)).thenReturn(emptyMovieList);

        // When
        ResponseMessage<List<MovieResponseCinema>> result = movieService.getMoviesByCinemaSlug(
                cinemaSlug, page, size, sort, type
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.MOVIE_NOT_FOUND);
        assertThat(result.getObject()).isNull();

        verify(movieRepository).findMoviesByCinemaSlug(cinemaSlug);
    }

    @Test
    @DisplayName("Should verify only repository is called when pagination parameters are ignored")
    void getMoviesByCinemaSlug_ShouldOnlyCallRepository() {
        // Given
        String cinemaSlug = "test-cinema";
        int page = 2;
        int size = 20;
        String sort = "releaseDate";
        String type = "desc";

        List<MovieResponseCinema> mockMovies = Arrays.asList(createMockMovieResponseCinema(1L, "Test Movie"));

        when(movieRepository.findMoviesByCinemaSlug(cinemaSlug)).thenReturn(mockMovies);

        // When
        movieService.getMoviesByCinemaSlug(cinemaSlug, page, size, sort, type);

        // Then
        verify(movieRepository).findMoviesByCinemaSlug(cinemaSlug);
        // Note: Pagination parameters are completely ignored in current implementation
    }

    @Test
    @DisplayName("Should handle empty string cinema slug")
    void getMoviesByCinemaSlug_WhenCinemaSlugIsEmpty_ShouldReturnNotFound() {
        // Given
        String cinemaSlug = "";
        int page = 0;
        int size = 10;
        String sort = "name";
        String type = "asc";

        List<MovieResponseCinema> emptyMovieList = Collections.emptyList();

        when(movieRepository.findMoviesByCinemaSlug(cinemaSlug)).thenReturn(emptyMovieList);

        // When
        ResponseMessage<List<MovieResponseCinema>> result = movieService.getMoviesByCinemaSlug(
                cinemaSlug, page, size, sort, type
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.MOVIE_NOT_FOUND);
        assertThat(result.getObject()).isNull();

        verify(movieRepository).findMoviesByCinemaSlug(cinemaSlug);
    }

    private MovieResponseCinema createMockMovieResponseCinema(Long id, String title) {
        // Assuming MovieResponseCinema has these fields - adjust based on your actual DTO
        return MovieResponseCinema.builder()
                .id(id)
                .title(title)
                .slug(title.toLowerCase().replace(" ", "-"))
                .summary("Test summary")
                // Add other required fields based on your actual DTO
                .build();
    }
}