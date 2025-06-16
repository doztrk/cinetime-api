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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieService - getAllMovies Tests")
class GetAllMoviesTest {

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

    @BeforeEach
    void setUp() {
        // Create test movies
        testMovie1 = Movie.builder()
                .id(1L)
                .title("The Dark Knight")
                .summary("Batman fights Joker")
                .slug("the-dark-knight")
                .releaseDate(LocalDate.of(2008, 7, 18))
                .duration(152)
                .director("Christopher Nolan")
                .genre(Arrays.asList("Action", "Crime", "Drama"))
                .status(MovieStatus.fromValue(1))
                .createdAt(LocalDateTime.now())
                .build();

        testMovie2 = Movie.builder()
                .id(2L)
                .title("Inception")
                .summary("Dreams within dreams")
                .slug("inception")
                .releaseDate(LocalDate.of(2010, 7, 16))
                .duration(148)
                .director("Christopher Nolan")
                .genre(Arrays.asList("Action", "Sci-Fi", "Thriller"))
                .status(MovieStatus.fromValue(1))
                .createdAt(LocalDateTime.now())
                .build();

        // Create test movie responses
        movieResponse1 = MovieResponse.builder()
                .id(1L)
                .title("The Dark Knight")
                .summary("Batman fights Joker")
                .slug("the-dark-knight")
                .releaseDate(LocalDate.of(2008, 7, 18))
                .duration(152)
                .director("Christopher Nolan")
                .genre(Arrays.asList("Action", "Crime", "Drama"))
                .status(MovieStatus.fromValue(1))
                .build();

        movieResponse2 = MovieResponse.builder()
                .id(2L)
                .title("Inception")
                .summary("Dreams within dreams")
                .slug("inception")
                .releaseDate(LocalDate.of(2010, 7, 16))
                .duration(148)
                .director("Christopher Nolan")
                .genre(Arrays.asList("Action", "Sci-Fi", "Thriller"))
                .status(MovieStatus.fromValue(1))
                .build();

        // Create test pageable
        testPageable = PageRequest.of(0, 10, Sort.by("title").ascending());
    }

    @Test
    @DisplayName("Should return movies successfully when movies exist")
    void getAllMovies_WithExistingMovies_ShouldReturnSuccessResponse() {
        // Given
        int page = 0;
        int size = 10;
        String sort = "title";
        String type = "asc";

        List<Movie> movieList = Arrays.asList(testMovie1, testMovie2);
        Page<Movie> moviePage = new PageImpl<>(movieList, testPageable, 2);

        List<MovieResponse> responseList = Arrays.asList(movieResponse1, movieResponse2);
        Page<MovieResponse> movieResponsePage = new PageImpl<>(responseList, testPageable, 2);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(movieRepository.findAll(testPageable)).thenReturn(moviePage);
        when(movieMapper.mapMovieToMovieResponse(testMovie1)).thenReturn(movieResponse1);
        when(movieMapper.mapMovieToMovieResponse(testMovie2)).thenReturn(movieResponse2);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getAllMovies(page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Movies found successfully");
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getContent()).hasSize(2);
        assertThat(result.getObject().getTotalElements()).isEqualTo(2);
        assertThat(result.getObject().getContent().get(0).getTitle()).isEqualTo("The Dark Knight");
        assertThat(result.getObject().getContent().get(1).getTitle()).isEqualTo("Inception");

        // Verify interactions
        verify(pageableHelper, times(1)).pageableSort(page, size, sort, type);
        verify(movieRepository, times(1)).findAll(testPageable);
        verify(movieMapper, times(1)).mapMovieToMovieResponse(testMovie1);
        verify(movieMapper, times(1)).mapMovieToMovieResponse(testMovie2);
    }

    @Test
    @DisplayName("Should return not found when no movies exist")
    void getAllMovies_WithNoMovies_ShouldReturnNotFoundResponse() {
        // Given
        int page = 0;
        int size = 10;
        String sort = "title";
        String type = "asc";

        Page<Movie> emptyMoviePage = new PageImpl<>(Collections.emptyList(), testPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(movieRepository.findAll(testPageable)).thenReturn(emptyMoviePage);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getAllMovies(page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.MOVIES_NOT_FOUND);
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getObject()).isNull();

        // Verify interactions
        verify(pageableHelper, times(1)).pageableSort(page, size, sort, type);
        verify(movieRepository, times(1)).findAll(testPageable);
        verify(movieMapper, times(0)).mapMovieToMovieResponse(any(Movie.class));
    }

    @Test
    @DisplayName("Should return single movie when only one movie exists")
    void getAllMovies_WithSingleMovie_ShouldReturnSingleMovieResponse() {
        // Given
        int page = 0;
        int size = 10;
        String sort = "title";
        String type = "asc";

        List<Movie> singleMovieList = Collections.singletonList(testMovie1);
        Page<Movie> singleMoviePage = new PageImpl<>(singleMovieList, testPageable, 1);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(movieRepository.findAll(testPageable)).thenReturn(singleMoviePage);
        when(movieMapper.mapMovieToMovieResponse(testMovie1)).thenReturn(movieResponse1);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getAllMovies(page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Movies found successfully");
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getContent()).hasSize(1);
        assertThat(result.getObject().getTotalElements()).isEqualTo(1);
        assertThat(result.getObject().getContent().get(0).getTitle()).isEqualTo("The Dark Knight");

        // Verify interactions
        verify(pageableHelper, times(1)).pageableSort(page, size, sort, type);
        verify(movieRepository, times(1)).findAll(testPageable);
        verify(movieMapper, times(1)).mapMovieToMovieResponse(testMovie1);
    }

    @Test
    @DisplayName("Should handle different pagination parameters correctly")
    void getAllMovies_WithDifferentPaginationParams_ShouldHandleCorrectly() {
        // Given
        int page = 1;
        int size = 5;
        String sort = "releaseDate";
        String type = "desc";

        Pageable customPageable = PageRequest.of(1, 5, Sort.by("releaseDate").descending());
        List<Movie> movieList = Arrays.asList(testMovie1, testMovie2);
        Page<Movie> moviePage = new PageImpl<>(movieList, customPageable, 10); // Total 10 elements

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(customPageable);
        when(movieRepository.findAll(customPageable)).thenReturn(moviePage);
        when(movieMapper.mapMovieToMovieResponse(testMovie1)).thenReturn(movieResponse1);
        when(movieMapper.mapMovieToMovieResponse(testMovie2)).thenReturn(movieResponse2);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getAllMovies(page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Movies found successfully");
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getContent()).hasSize(2);
        assertThat(result.getObject().getTotalElements()).isEqualTo(10);
        assertThat(result.getObject().getNumber()).isEqualTo(1); // Current page
        assertThat(result.getObject().getSize()).isEqualTo(5); // Page size

        // Verify interactions
        verify(pageableHelper, times(1)).pageableSort(page, size, sort, type);
        verify(movieRepository, times(1)).findAll(customPageable);
        verify(movieMapper, times(2)).mapMovieToMovieResponse(any(Movie.class));
    }

    @Test
    @DisplayName("Should preserve pagination metadata in response")
    void getAllMovies_ShouldPreservePaginationMetadata() {
        // Given
        int page = 2;
        int size = 3;
        String sort = "id";
        String type = "asc";

        Pageable customPageable = PageRequest.of(2, 3, Sort.by("id").ascending());
        List<Movie> movieList = Collections.singletonList(testMovie1);
        Page<Movie> moviePage = new PageImpl<>(movieList, customPageable, 15); // Total 15 elements

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(customPageable);
        when(movieRepository.findAll(customPageable)).thenReturn(moviePage);
        when(movieMapper.mapMovieToMovieResponse(testMovie1)).thenReturn(movieResponse1);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getAllMovies(page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getObject()).isNotNull();

        Page<MovieResponse> responsePage = result.getObject();
        assertThat(responsePage.getNumber()).isEqualTo(2); // Current page
        assertThat(responsePage.getSize()).isEqualTo(3); // Page size
        assertThat(responsePage.getTotalElements()).isEqualTo(15); // Total elements
        assertThat(responsePage.getTotalPages()).isEqualTo(5); // Total pages (15/3)
        assertThat(responsePage.isFirst()).isFalse();
        assertThat(responsePage.isLast()).isFalse();
        assertThat(responsePage.hasNext()).isTrue();
        assertThat(responsePage.hasPrevious()).isTrue();
    }
}