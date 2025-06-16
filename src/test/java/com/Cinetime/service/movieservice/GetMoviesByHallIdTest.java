package com.Cinetime.service.movieservice;

import com.Cinetime.entity.Movie;
import com.Cinetime.enums.MovieStatus;
import com.Cinetime.helpers.PageableHelper;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieService - getMoviesByHallId Tests")
class GetMoviesByHallIdTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private PageableHelper pageableHelper;

    @Mock
    private MovieMapper movieMapper;

    @InjectMocks
    private MovieService movieService;

    private Movie testMovie1;
    private Movie testMovie2;
    private MovieResponse movieResponse1;
    private MovieResponse movieResponse2;
    private Pageable testPageable;
    private Long testHallId;

    @BeforeEach
    void setUp() {
        testHallId = 1L;

        // Create test movies
        testMovie1 = Movie.builder()
                .id(1L)
                .title("Test Movie 1")
                .summary("Test Summary 1")
                .slug("test-movie-1")
                .releaseDate(LocalDate.now())
                .duration(120)
                .director("Test Director 1")
                .genre(Arrays.asList("Action", "Drama"))
                .status(MovieStatus.fromValue(1))
                .build();

        testMovie2 = Movie.builder()
                .id(2L)
                .title("Test Movie 2")
                .summary("Test Summary 2")
                .slug("test-movie-2")
                .releaseDate(LocalDate.now().minusDays(1))
                .duration(150)
                .director("Test Director 2")
                .genre(Collections.singletonList("Comedy"))
                .status(MovieStatus.fromValue(1))
                .build();

        // Create test movie responses
        movieResponse1 = MovieResponse.builder()
                .id(1L)
                .title("Test Movie 1")
                .summary("Test Summary 1")
                .slug("test-movie-1")
                .releaseDate(LocalDate.now())
                .duration(120)
                .director("Test Director 1")
                .genre(Arrays.asList("Action", "Drama"))
                .status(MovieStatus.fromValue(1))
                .build();

        movieResponse2 = MovieResponse.builder()
                .id(2L)
                .title("Test Movie 2")
                .summary("Test Summary 2")
                .slug("test-movie-2")
                .releaseDate(LocalDate.now().minusDays(1))
                .duration(150)
                .director("Test Director 2")
                .genre(Collections.singletonList("Comedy"))
                .status(MovieStatus.fromValue(1))
                .build();

        testPageable = PageRequest.of(0, 10, Sort.by("title").ascending());
    }

    @Test
    @DisplayName("Should return movies successfully when hall has movies")
    void getMoviesByHallId_WithExistingMovies_ShouldReturnMoviesSuccessfully() {
        // Given
        int page = 0;
        int size = 10;
        String sort = "title";
        String type = "asc";

        List<Movie> movieList = Arrays.asList(testMovie1, testMovie2);
        Page<Movie> moviePage = new PageImpl<>(movieList, testPageable, movieList.size());

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(movieRepository.findMoviesByHallId(testHallId, testPageable)).thenReturn(moviePage);
        when(movieMapper.mapMovieToMovieResponse(testMovie1)).thenReturn(movieResponse1);
        when(movieMapper.mapMovieToMovieResponse(testMovie2)).thenReturn(movieResponse2);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getMoviesByHallId(testHallId, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo("Movies found successfully");
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getContent()).hasSize(2);
        assertThat(result.getObject().getContent().get(0).getId()).isEqualTo(1L);
        assertThat(result.getObject().getContent().get(0).getTitle()).isEqualTo("Test Movie 1");
        assertThat(result.getObject().getContent().get(1).getId()).isEqualTo(2L);
        assertThat(result.getObject().getContent().get(1).getTitle()).isEqualTo("Test Movie 2");
        assertThat(result.getObject().getTotalElements()).isEqualTo(2);

        // Verify interactions
        verify(pageableHelper, times(1)).pageableSort(page, size, sort, type);
        verify(movieRepository, times(1)).findMoviesByHallId(testHallId, testPageable);
        verify(movieMapper, times(1)).mapMovieToMovieResponse(testMovie1);
        verify(movieMapper, times(1)).mapMovieToMovieResponse(testMovie2);
    }

    @Test
    @DisplayName("Should return single movie when hall has one movie")
    void getMoviesByHallId_WithSingleMovie_ShouldReturnSingleMovie() {
        // Given
        int page = 0;
        int size = 10;
        String sort = "title";
        String type = "asc";

        List<Movie> movieList = Collections.singletonList(testMovie1);
        Page<Movie> moviePage = new PageImpl<>(movieList, testPageable, movieList.size());

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(movieRepository.findMoviesByHallId(testHallId, testPageable)).thenReturn(moviePage);
        when(movieMapper.mapMovieToMovieResponse(testMovie1)).thenReturn(movieResponse1);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getMoviesByHallId(testHallId, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo("Movies found successfully");
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getContent()).hasSize(1);
        assertThat(result.getObject().getContent().get(0).getId()).isEqualTo(1L);
        assertThat(result.getObject().getTotalElements()).isEqualTo(1);

        // Verify interactions
        verify(pageableHelper, times(1)).pageableSort(page, size, sort, type);
        verify(movieRepository, times(1)).findMoviesByHallId(testHallId, testPageable);
        verify(movieMapper, times(1)).mapMovieToMovieResponse(testMovie1);
    }

    @Test
    @DisplayName("Should return NO_CONTENT when no movies found for hall")
    void getMoviesByHallId_WithNoMovies_ShouldReturnNoContent() {
        // Given
        int page = 0;
        int size = 10;
        String sort = "title";
        String type = "asc";

        Page<Movie> emptyMoviePage = new PageImpl<>(Collections.emptyList(), testPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(movieRepository.findMoviesByHallId(testHallId, testPageable)).thenReturn(emptyMoviePage);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getMoviesByHallId(testHallId, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.MOVIES_NOT_FOUND);
        assertThat(result.getObject()).isNull();

        // Verify interactions
        verify(pageableHelper, times(1)).pageableSort(page, size, sort, type);
        verify(movieRepository, times(1)).findMoviesByHallId(testHallId, testPageable);
        verify(movieMapper, never()).mapMovieToMovieResponse(any(Movie.class));
    }

    @Test
    @DisplayName("Should handle different page parameters correctly")
    void getMoviesByHallId_WithDifferentPageParameters_ShouldHandleCorrectly() {
        // Given
        int page = 1;
        int size = 5;
        String sort = "releaseDate";
        String type = "desc";

        Pageable customPageable = PageRequest.of(page, size, Sort.by(sort).descending());
        List<Movie> movieList = Collections.singletonList(testMovie1);
        Page<Movie> moviePage = new PageImpl<>(movieList, customPageable, movieList.size());

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(customPageable);
        when(movieRepository.findMoviesByHallId(testHallId, customPageable)).thenReturn(moviePage);
        when(movieMapper.mapMovieToMovieResponse(testMovie1)).thenReturn(movieResponse1);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getMoviesByHallId(testHallId, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo("Movies found successfully");
        assertThat(result.getObject()).isNotNull();

        // Verify interactions with correct parameters
        verify(pageableHelper, times(1)).pageableSort(page, size, sort, type);
        verify(movieRepository, times(1)).findMoviesByHallId(testHallId, customPageable);
        verify(movieMapper, times(1)).mapMovieToMovieResponse(testMovie1);
    }

    @Test
    @DisplayName("Should handle different hall IDs correctly")
    void getMoviesByHallId_WithDifferentHallIds_ShouldHandleCorrectly() {
        // Given
        Long differentHallId = 999L;
        int page = 0;
        int size = 10;
        String sort = "title";
        String type = "asc";

        Page<Movie> emptyMoviePage = new PageImpl<>(Collections.emptyList(), testPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(movieRepository.findMoviesByHallId(differentHallId, testPageable)).thenReturn(emptyMoviePage);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getMoviesByHallId(differentHallId, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.MOVIES_NOT_FOUND);

        // Verify interactions with correct hall ID
        verify(pageableHelper, times(1)).pageableSort(page, size, sort, type);
        verify(movieRepository, times(1)).findMoviesByHallId(differentHallId, testPageable);
        verify(movieMapper, never()).mapMovieToMovieResponse(any(Movie.class));
    }

    @Test
    @DisplayName("Should verify mapper is called for each movie in successful response")
    void getMoviesByHallId_WithMultipleMovies_ShouldCallMapperForEachMovie() {
        // Given
        int page = 0;
        int size = 10;
        String sort = "title";
        String type = "asc";

        List<Movie> movieList = Arrays.asList(testMovie1, testMovie2);
        Page<Movie> moviePage = new PageImpl<>(movieList, testPageable, movieList.size());

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(movieRepository.findMoviesByHallId(testHallId, testPageable)).thenReturn(moviePage);
        when(movieMapper.mapMovieToMovieResponse(any(Movie.class)))
                .thenReturn(movieResponse1)
                .thenReturn(movieResponse2);

        // When
        movieService.getMoviesByHallId(testHallId, page, size, sort, type);

        // Then - Verify mapper called exactly twice
        verify(movieMapper, times(2)).mapMovieToMovieResponse(any(Movie.class));
        verify(movieMapper, times(1)).mapMovieToMovieResponse(testMovie1);
        verify(movieMapper, times(1)).mapMovieToMovieResponse(testMovie2);
    }
}