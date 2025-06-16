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
@DisplayName("MovieService - getComingSoonMovies Tests")
class GetComingSoonMoviesTest {

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
    private MovieResponse testMovieResponse1;
    private MovieResponse testMovieResponse2;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        // Setup test data
        testMovie1 = Movie.builder()
                .id(1L)
                .title("Upcoming Movie 1")
                .slug("upcoming-movie-1")
                .summary("A great upcoming movie")
                .releaseDate(LocalDate.now().plusDays(30))
                .duration(120)
                .rating(8.5)
                .director("Director 1")
                .cast(Arrays.asList("Actor 1", "Actor 2"))
                .formats(Arrays.asList("IMAX", "Standard"))
                .genre(Arrays.asList("Action", "Adventure"))
                .status(MovieStatus.COMING_SOON)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testMovie2 = Movie.builder()
                .id(2L)
                .title("Upcoming Movie 2")
                .slug("upcoming-movie-2")
                .summary("Another great upcoming movie")
                .releaseDate(LocalDate.now().plusDays(60))
                .duration(150)
                .rating(9.0)
                .director("Director 2")
                .cast(Arrays.asList("Actor 3", "Actor 4"))
                .formats(Arrays.asList("4DX", "Standard"))
                .genre(Arrays.asList("Sci-Fi", "Thriller"))
                .status(MovieStatus.COMING_SOON)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testMovieResponse1 = MovieResponse.builder()
                .id(1L)
                .title("Upcoming Movie 1")
                .slug("upcoming-movie-1")
                .summary("A great upcoming movie")
                .releaseDate(LocalDate.now().plusDays(30))
                .duration(120)
                .rating(8.5)
                .director("Director 1")
                .cast(Arrays.asList("Actor 1", "Actor 2"))
                .formats(Arrays.asList("IMAX", "Standard"))
                .genre(Arrays.asList("Action", "Adventure"))
                .status(MovieStatus.COMING_SOON)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testMovieResponse2 = MovieResponse.builder()
                .id(2L)
                .title("Upcoming Movie 2")
                .slug("upcoming-movie-2")
                .summary("Another great upcoming movie")
                .releaseDate(LocalDate.now().plusDays(60))
                .duration(150)
                .rating(9.0)
                .director("Director 2")
                .cast(Arrays.asList("Actor 3", "Actor 4"))
                .formats(Arrays.asList("4DX", "Standard"))
                .genre(Arrays.asList("Sci-Fi", "Thriller"))
                .status(MovieStatus.COMING_SOON)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testPageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("Should return coming soon movies successfully when movies exist")
    void getComingSoonMovies_WhenMoviesExist_ShouldReturnSuccessResponse() {
        // Given
        int page = 0, size = 10;
        String sort = "releaseDate", type = "asc";

        List<Movie> movieList = Arrays.asList(testMovie1, testMovie2);
        Page<Movie> moviePage = new PageImpl<>(movieList, testPageable, movieList.size());
        Page<MovieResponse> movieResponsePage = new PageImpl<>(
                Arrays.asList(testMovieResponse1, testMovieResponse2),
                testPageable,
                movieList.size()
        );

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(movieRepository.findByStatus(MovieStatus.COMING_SOON, testPageable)).thenReturn(moviePage);
        when(movieMapper.mapMoviePageToMovieResponse(moviePage)).thenReturn(movieResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getComingSoonMovies(page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo("Movies found successfully");
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getContent()).hasSize(2);
        assertThat(result.getObject().getContent().get(0).getTitle()).isEqualTo("Upcoming Movie 1");
        assertThat(result.getObject().getContent().get(1).getTitle()).isEqualTo("Upcoming Movie 2");

        // Verify interactions
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(movieRepository).findByStatus(MovieStatus.COMING_SOON, testPageable);
        verify(movieMapper).mapMoviePageToMovieResponse(moviePage);
    }

    @Test
    @DisplayName("Should return NOT_FOUND when no coming soon movies exist")
    void getComingSoonMovies_WhenNoMoviesExist_ShouldReturnNotFoundResponse() {
        // Given
        int page = 0, size = 10;
        String sort = "releaseDate", type = "asc";

        Page<Movie> emptyMoviePage = new PageImpl<>(Collections.emptyList(), testPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(movieRepository.findByStatus(MovieStatus.COMING_SOON, testPageable)).thenReturn(emptyMoviePage);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getComingSoonMovies(page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.MOVIE_NOT_FOUND);
        assertThat(result.getObject()).isNull();

        // Verify interactions
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(movieRepository).findByStatus(MovieStatus.COMING_SOON, testPageable);
        verify(movieMapper, never()).mapMoviePageToMovieResponse(any());
    }

    @Test
    @DisplayName("Should handle single movie result correctly")
    void getComingSoonMovies_WhenSingleMovieExists_ShouldReturnSuccessResponse() {
        // Given
        int page = 0, size = 10;
        String sort = "title", type = "desc";

        List<Movie> singleMovieList = Collections.singletonList(testMovie1);
        Page<Movie> singleMoviePage = new PageImpl<>(singleMovieList, testPageable, 1);
        Page<MovieResponse> singleMovieResponsePage = new PageImpl<>(
                Collections.singletonList(testMovieResponse1),
                testPageable,
                1
        );

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(movieRepository.findByStatus(MovieStatus.COMING_SOON, testPageable)).thenReturn(singleMoviePage);
        when(movieMapper.mapMoviePageToMovieResponse(singleMoviePage)).thenReturn(singleMovieResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getComingSoonMovies(page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo("Movies found successfully");
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getContent()).hasSize(1);
        assertThat(result.getObject().getContent().get(0).getTitle()).isEqualTo("Upcoming Movie 1");

        // Verify interactions
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(movieRepository).findByStatus(MovieStatus.COMING_SOON, testPageable);
        verify(movieMapper).mapMoviePageToMovieResponse(singleMoviePage);
    }

    @Test
    @DisplayName("Should handle different pagination parameters correctly")
    void getComingSoonMovies_WithDifferentPaginationParams_ShouldPassCorrectParameters() {
        // Given
        int page = 2, size = 5;
        String sort = "title", type = "desc";
        Pageable customPageable = PageRequest.of(page, size);

        Page<Movie> emptyMoviePage = new PageImpl<>(Collections.emptyList(), customPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(customPageable);
        when(movieRepository.findByStatus(MovieStatus.COMING_SOON, customPageable)).thenReturn(emptyMoviePage);

        // When
        movieService.getComingSoonMovies(page, size, sort, type);

        // Then - Verify that the exact parameters were passed
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(movieRepository).findByStatus(MovieStatus.COMING_SOON, customPageable);
    }

    @Test
    @DisplayName("Should verify mapper is not called when no movies found")
    void getComingSoonMovies_WhenNoMovies_ShouldNotCallMapper() {
        // Given
        int page = 0, size = 10;
        String sort = "releaseDate", type = "asc";

        Page<Movie> emptyMoviePage = new PageImpl<>(Collections.emptyList(), testPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(movieRepository.findByStatus(MovieStatus.COMING_SOON, testPageable)).thenReturn(emptyMoviePage);

        // When
        movieService.getComingSoonMovies(page, size, sort, type);

        // Then - Mapper should never be called when no movies are found
        verify(movieMapper, never()).mapMoviePageToMovieResponse(any());
    }

    @Test
    @DisplayName("Should ensure correct method calls order")
    void getComingSoonMovies_ShouldCallMethodsInCorrectOrder() {
        // Given
        int page = 0, size = 10;
        String sort = "releaseDate", type = "asc";

        List<Movie> movieList = Collections.singletonList(testMovie1);
        Page<Movie> moviePage = new PageImpl<>(movieList, testPageable, 1);
        Page<MovieResponse> movieResponsePage = new PageImpl<>(
                Collections.singletonList(testMovieResponse1),
                testPageable,
                1
        );

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(movieRepository.findByStatus(MovieStatus.COMING_SOON, testPageable)).thenReturn(moviePage);
        when(movieMapper.mapMoviePageToMovieResponse(moviePage)).thenReturn(movieResponsePage);

        // When
        movieService.getComingSoonMovies(page, size, sort, type);

        // Then - Verify method calls order using InOrder
        var inOrder = inOrder(pageableHelper, movieRepository, movieMapper);
        inOrder.verify(pageableHelper).pageableSort(page, size, sort, type);
        inOrder.verify(movieRepository).findByStatus(MovieStatus.COMING_SOON, testPageable);
        inOrder.verify(movieMapper).mapMoviePageToMovieResponse(moviePage);
    }
}