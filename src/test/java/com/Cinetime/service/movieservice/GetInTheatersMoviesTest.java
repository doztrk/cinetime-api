package com.Cinetime.service;

import com.Cinetime.entity.Movie;
import com.Cinetime.enums.MovieStatus;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.payload.dto.response.MovieResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.mappers.MovieMapper;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.repo.MovieRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieService - getInTheatersMovies Tests")
class GetInTheatersMoviesTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private PageableHelper pageableHelper;

    @Mock
    private MovieMapper movieMapper;

    @InjectMocks
    private MovieService movieService;

    private Pageable mockPageable;
    private Movie testMovie1;
    private Movie testMovie2;
    private MovieResponse testMovieResponse1;
    private MovieResponse testMovieResponse2;

    @BeforeEach
    void setUp() {
        mockPageable = PageRequest.of(0, 10);

        // Create test movies
        testMovie1 = Movie.builder()
                .id(1L)
                .title("Test Movie 1")
                .slug("test-movie-1")
                .summary("Test summary 1")
                .releaseDate(LocalDate.now())
                .duration(120)
                .rating(8.5)
                .director("Test Director 1")
                .cast(Arrays.asList("Actor 1", "Actor 2"))
                .formats(Arrays.asList("IMAX", "2D"))
                .genre(Arrays.asList("Action", "Drama"))
                .status(MovieStatus.IN_THEATERS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testMovie2 = Movie.builder()
                .id(2L)
                .title("Test Movie 2")
                .slug("test-movie-2")
                .summary("Test summary 2")
                .releaseDate(LocalDate.now())
                .duration(90)
                .rating(7.5)
                .director("Test Director 2")
                .cast(Arrays.asList("Actor 3", "Actor 4"))
                .formats(Arrays.asList("3D", "IMAX"))
                .genre(Arrays.asList("Comedy", "Romance"))
                .status(MovieStatus.IN_THEATERS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Create test movie responses
        testMovieResponse1 = MovieResponse.builder()
                .id(1L)
                .title("Test Movie 1")
                .slug("test-movie-1")
                .summary("Test summary 1")
                .releaseDate(LocalDate.now())
                .duration(120)
                .rating(8.5)
                .director("Test Director 1")
                .cast(Arrays.asList("Actor 1", "Actor 2"))
                .formats(Arrays.asList("IMAX", "2D"))
                .genre(Arrays.asList("Action", "Drama"))
                .status(MovieStatus.IN_THEATERS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testMovieResponse2 = MovieResponse.builder()
                .id(2L)
                .title("Test Movie 2")
                .slug("test-movie-2")
                .summary("Test summary 2")
                .releaseDate(LocalDate.now())
                .duration(90)
                .rating(7.5)
                .director("Test Director 2")
                .cast(Arrays.asList("Actor 3", "Actor 4"))
                .formats(Arrays.asList("3D", "IMAX"))
                .genre(Arrays.asList("Comedy", "Romance"))
                .status(MovieStatus.IN_THEATERS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should return movies successfully when movies exist in theaters")
    void shouldReturnMoviesSuccessfully_WhenMoviesExistInTheaters() {
        // Given
        int page = 0, size = 10;
        String sort = "title", type = "asc";

        List<Movie> movieList = Arrays.asList(testMovie1, testMovie2);
        Page<Movie> moviePage = new PageImpl<>(movieList, mockPageable, movieList.size());
        Page<MovieResponse> movieResponsePage = new PageImpl<>(
                Arrays.asList(testMovieResponse1, testMovieResponse2),
                mockPageable,
                movieList.size()
        );

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(mockPageable);
        when(movieRepository.findByStatus(MovieStatus.IN_THEATERS, mockPageable)).thenReturn(moviePage);
        when(movieMapper.mapMoviePageToMovieResponse(moviePage)).thenReturn(movieResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getInTheatersMovies(page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo("Movies found successfully");
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getContent()).hasSize(2);
        assertThat(result.getObject().getContent().get(0).getTitle()).isEqualTo("Test Movie 1");
        assertThat(result.getObject().getContent().get(1).getTitle()).isEqualTo("Test Movie 2");

        // Verify interactions
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(movieRepository).findByStatus(MovieStatus.IN_THEATERS, mockPageable);
        verify(movieMapper).mapMoviePageToMovieResponse(moviePage);
    }

    @Test
    @DisplayName("Should return NOT_FOUND when no movies are in theaters")
    void shouldReturnNotFound_WhenNoMoviesInTheaters() {
        // Given
        int page = 0, size = 10;
        String sort = "title", type = "asc";

        Page<Movie> emptyMoviePage = new PageImpl<>(Collections.emptyList(), mockPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(mockPageable);
        when(movieRepository.findByStatus(MovieStatus.IN_THEATERS, mockPageable)).thenReturn(emptyMoviePage);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getInTheatersMovies(page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.MOVIE_NOT_FOUND);
        assertThat(result.getObject()).isNull();

        // Verify interactions
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(movieRepository).findByStatus(MovieStatus.IN_THEATERS, mockPageable);
        verify(movieMapper, never()).mapMoviePageToMovieResponse(any());
    }

    @Test
    @DisplayName("Should handle different pagination parameters correctly")
    void shouldHandleDifferentPaginationParameters() {
        // Given
        int page = 2, size = 5;
        String sort = "releaseDate", type = "desc";

        Pageable customPageable = PageRequest.of(page, size);
        List<Movie> movieList = Arrays.asList(testMovie1);
        Page<Movie> moviePage = new PageImpl<>(movieList, customPageable, 1);
        Page<MovieResponse> movieResponsePage = new PageImpl<>(
                Arrays.asList(testMovieResponse1),
                customPageable,
                1
        );

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(customPageable);
        when(movieRepository.findByStatus(MovieStatus.IN_THEATERS, customPageable)).thenReturn(moviePage);
        when(movieMapper.mapMoviePageToMovieResponse(moviePage)).thenReturn(movieResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getInTheatersMovies(page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject().getContent()).hasSize(1);

        // Verify correct parameters were passed
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(movieRepository).findByStatus(MovieStatus.IN_THEATERS, customPageable);
    }

    @Test
    @DisplayName("Should handle default parameters correctly")
    void shouldHandleDefaultParameters() {
        // Given - using defaults that would be set by controller
        int page = 0, size = 10;
        String sort = "releaseDate", type = "asc";

        List<Movie> movieList = Arrays.asList(testMovie1, testMovie2);
        Page<Movie> moviePage = new PageImpl<>(movieList, mockPageable, movieList.size());
        Page<MovieResponse> movieResponsePage = new PageImpl<>(
                Arrays.asList(testMovieResponse1, testMovieResponse2),
                mockPageable,
                movieList.size()
        );

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(mockPageable);
        when(movieRepository.findByStatus(MovieStatus.IN_THEATERS, mockPageable)).thenReturn(moviePage);
        when(movieMapper.mapMoviePageToMovieResponse(moviePage)).thenReturn(movieResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getInTheatersMovies(page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject().getContent()).hasSize(2);

        verify(pageableHelper).pageableSort(page, size, sort, type);
    }

    @Test
    @DisplayName("Should verify MovieStatus.IN_THEATERS is used correctly")
    void shouldVerifyCorrectMovieStatusIsUsed() {
        // Given
        int page = 0, size = 10;
        String sort = "title", type = "asc";

        Page<Movie> emptyMoviePage = new PageImpl<>(Collections.emptyList(), mockPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(mockPageable);
        when(movieRepository.findByStatus(MovieStatus.IN_THEATERS, mockPageable)).thenReturn(emptyMoviePage);

        // When
        movieService.getInTheatersMovies(page, size, sort, type);

        // Then
        verify(movieRepository).findByStatus(eq(MovieStatus.IN_THEATERS), any(Pageable.class));
        verify(movieRepository, never()).findByStatus(eq(MovieStatus.COMING_SOON), any(Pageable.class));
        verify(movieRepository, never()).findByStatus(eq(MovieStatus.ENDED), any(Pageable.class));
    }

    @Test
    @DisplayName("Should maintain page metadata in response")
    void shouldMaintainPageMetadataInResponse() {
        // Given
        int page = 1, size = 5;
        String sort = "title", type = "asc";

        Pageable customPageable = PageRequest.of(page, size);
        List<Movie> movieList = Arrays.asList(testMovie1, testMovie2);
        Page<Movie> moviePage = new PageImpl<>(movieList, customPageable, 10); // Total 10 items
        Page<MovieResponse> movieResponsePage = new PageImpl<>(
                Arrays.asList(testMovieResponse1, testMovieResponse2),
                customPageable,
                10
        );

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(customPageable);
        when(movieRepository.findByStatus(MovieStatus.IN_THEATERS, customPageable)).thenReturn(moviePage);
        when(movieMapper.mapMoviePageToMovieResponse(moviePage)).thenReturn(movieResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getInTheatersMovies(page, size, sort, type);

        // Then
        Page<MovieResponse> resultPage = result.getObject();
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getNumber()).isEqualTo(1); // Current page
        assertThat(resultPage.getSize()).isEqualTo(5); // Page size
        assertThat(resultPage.getTotalElements()).isEqualTo(10); // Total items
        assertThat(resultPage.getTotalPages()).isEqualTo(2); // Total pages
        assertThat(resultPage.getContent()).hasSize(2); // Items in this page
    }
}