package com.Cinetime.service.movieservice;

import com.Cinetime.entity.Movie;
import com.Cinetime.enums.MovieStatus;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.payload.dto.response.MovieResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.mappers.MovieMapper;
import com.Cinetime.repo.MovieRepository;
import com.Cinetime.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetMoviesByQueryTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private PageableHelper pageableHelper;

    @Mock
    private MovieMapper movieMapper;

    @InjectMocks
    private MovieService movieService;

    private Movie mockMovie;
    private MovieResponse mockMovieResponse;
    private Page<Movie> mockMoviePage;
    private Page<MovieResponse> mockMovieResponsePage;
    private Pageable mockPageable;

    @BeforeEach
    void setUp() {
        // Mock Movie entity
        mockMovie = Movie.builder()
                .id(1L)
                .title("Test Movie")
                .summary("Test Summary")
                .slug("test-movie")
                .releaseDate(LocalDate.now())
                .duration(120)
                .director("Test Director")
                .genre(Collections.singletonList("Action"))
                .status(MovieStatus.fromValue(1))
                .build();

        // Mock MovieResponse DTO
        mockMovieResponse = MovieResponse.builder()
                .id(1L)
                .title("Test Movie")
                .summary("Test Summary")
                .slug("test-movie")
                .releaseDate(LocalDate.now())
                .duration(120)
                .director("Test Director")
                .genre(Collections.singletonList("Action"))
                .status(MovieStatus.fromValue(1))
                .build();

        // Mock Pageable
        mockPageable = PageRequest.of(0, 10, Sort.by("title").ascending());

        // Mock Page objects
        mockMoviePage = new PageImpl<>(List.of(mockMovie), mockPageable, 1);
        mockMovieResponsePage = new PageImpl<>(List.of(mockMovieResponse), mockPageable, 1);
    }

    @Test
    @DisplayName("Should return movies when valid query is provided")
    void getMoviesByQuery_WithValidQuery_ShouldReturnMovies() {
        // Given
        String query = "test";
        int page = 0;
        int size = 10;
        String sort = "title";
        String type = "asc";

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(mockPageable);
        when(movieRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, mockPageable))
                .thenReturn(mockMoviePage);
        when(movieMapper.mapMoviePageToMovieResponse(mockMoviePage)).thenReturn(mockMovieResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getMoviesByQuery(query, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Movies found successfully");
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject()).isEqualTo(mockMovieResponsePage);
        assertThat(result.getObject().getContent()).hasSize(1);
        assertThat(result.getObject().getContent().get(0).getTitle()).isEqualTo("Test Movie");

        // Verify interactions
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(movieRepository).findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, mockPageable);
        verify(movieMapper).mapMoviePageToMovieResponse(mockMoviePage);
    }

    @Test
    @DisplayName("Should return empty page when no movies match the query")
    void getMoviesByQuery_WithNoMatches_ShouldReturnEmptyPage() {
        // Given
        String query = "nonexistent";
        int page = 0;
        int size = 10;
        String sort = "title";
        String type = "asc";

        Page<Movie> emptyMoviePage = new PageImpl<>(Collections.emptyList(), mockPageable, 0);
        Page<MovieResponse> emptyMovieResponsePage = new PageImpl<>(Collections.emptyList(), mockPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(mockPageable);
        when(movieRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, mockPageable))
                .thenReturn(emptyMoviePage);
        when(movieMapper.mapMoviePageToMovieResponse(emptyMoviePage)).thenReturn(emptyMovieResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getMoviesByQuery(query, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Movies found successfully");
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject().getContent()).isEmpty();
        assertThat(result.getObject().getTotalElements()).isEqualTo(0);

        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(movieRepository).findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, mockPageable);
        verify(movieMapper).mapMoviePageToMovieResponse(emptyMoviePage);
    }


    @Test
    @DisplayName("Should handle empty query parameter")
    void getMoviesByQuery_WithEmptyQuery_ShouldReturnAllMovies() {
        // Given
        String query = "";
        int page = 0;
        int size = 10;
        String sort = "title";
        String type = "asc";

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(mockPageable);
        when(movieRepository.findAll(mockPageable)).thenReturn(mockMoviePage);
        when(movieMapper.mapMoviePageToMovieResponse(mockMoviePage)).thenReturn(mockMovieResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getMoviesByQuery(query, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Movies found successfully");
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject()).isEqualTo(mockMovieResponsePage);

        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(movieRepository).findAll(mockPageable);
        verify(movieMapper).mapMoviePageToMovieResponse(mockMoviePage);
        verify(movieRepository, never()).findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(anyString(), anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should handle repository exception")
    void getMoviesByQuery_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Given
        String query = "test";
        int page = 0;
        int size = 10;
        String sort = "title";
        String type = "asc";

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(mockPageable);
        when(movieRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, mockPageable))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> movieService.getMoviesByQuery(query, page, size, sort, type))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection failed");

        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(movieRepository).findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, mockPageable);
        verify(movieMapper, never()).mapMoviePageToMovieResponse(any());
    }

    @Test
    @DisplayName("Should handle case-insensitive search correctly")
    void getMoviesByQuery_WithMixedCaseQuery_ShouldReturnMovies() {
        // Given
        String query = "TeSt";
        int page = 0;
        int size = 10;
        String sort = "title";
        String type = "asc";

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(mockPageable);
        when(movieRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, mockPageable))
                .thenReturn(mockMoviePage);
        when(movieMapper.mapMoviePageToMovieResponse(mockMoviePage)).thenReturn(mockMovieResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getMoviesByQuery(query, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject().getContent()).hasSize(1);

        verify(movieRepository).findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, mockPageable);
    }

    @Test
    @DisplayName("Should pass correct pagination parameters")
    void getMoviesByQuery_WithCustomPagination_ShouldUseCorrectParameters() {
        // Given
        String query = "test";
        int page = 2;
        int size = 20;
        String sort = "releaseDate";
        String type = "desc";

        Pageable customPageable = PageRequest.of(2, 20, Sort.by("releaseDate").descending());

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(customPageable);
        when(movieRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, customPageable))
                .thenReturn(mockMoviePage);
        when(movieMapper.mapMoviePageToMovieResponse(mockMoviePage)).thenReturn(mockMovieResponsePage);

        // When
        movieService.getMoviesByQuery(query, page, size, sort, type);

        // Then
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(movieRepository).findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, customPageable);
    }
}