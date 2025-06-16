package com.Cinetime.service.movieservice;

import com.Cinetime.entity.Movie;
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
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAllMoviesByPage {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private PageableHelper pageableHelper;

    @Mock
    private MovieMapper movieMapper;

    @InjectMocks
    private MovieService movieService; // Assuming your service class name

    private Movie mockMovie;
    private MovieResponse mockMovieResponse;
    private Pageable mockPageable;

    @BeforeEach
    void setUp() {
        mockMovie = Movie.builder()
                .id(1L)
                .title("Test Movie")
                .build();

        mockMovieResponse = MovieResponse.builder()
                .id(1L)
                .title("Test Movie")
                .build();

        mockPageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("Should return movies successfully when movies exist for hall")
    void getMoviesByHallId_WhenMoviesExist_ShouldReturnMoviesSuccessfully() {
        // Given
        Long hallId = 1L;
        int page = 0;
        int size = 10;
        String sort = "title";
        String type = "ASC";

        List<Movie> movies = Arrays.asList(mockMovie);
        Page<Movie> moviePage = new PageImpl<>(movies, mockPageable, 1);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(mockPageable);
        when(movieRepository.findMoviesByHallId(hallId, mockPageable)).thenReturn(moviePage);
        when(movieMapper.mapMovieToMovieResponse(mockMovie)).thenReturn(mockMovieResponse);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getMoviesByHallId(hallId, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Movies found successfully");
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getContent()).hasSize(1);
        assertThat(result.getObject().getContent().get(0).getTitle()).isEqualTo("Test Movie");

        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(movieRepository).findMoviesByHallId(hallId, mockPageable);
        verify(movieMapper).mapMovieToMovieResponse(mockMovie);
    }

    @Test
    @DisplayName("Should return NO_CONTENT when no movies found for hall")
    void getMoviesByHallId_WhenNoMoviesExist_ShouldReturnNoContent() {
        // Given
        Long hallId = 1L;
        int page = 0;
        int size = 10;
        String sort = "title";
        String type = "ASC";

        Page<Movie> emptyPage = new PageImpl<>(Collections.emptyList(), mockPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(mockPageable);
        when(movieRepository.findMoviesByHallId(hallId, mockPageable)).thenReturn(emptyPage);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getMoviesByHallId(hallId, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.MOVIES_NOT_FOUND);
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getObject()).isNull();

        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(movieRepository).findMoviesByHallId(hallId, mockPageable);
        verifyNoInteractions(movieMapper);
    }

    @Test
    @DisplayName("Should handle different pagination parameters correctly")
    void getMoviesByHallId_WithDifferentPaginationParams_ShouldWork() {
        // Given
        Long hallId = 1L;
        int page = 2;
        int size = 5;
        String sort = "releaseDate";
        String type = "DESC";

        Pageable customPageable = PageRequest.of(page, size);
        List<Movie> movies = Arrays.asList(mockMovie);
        Page<Movie> moviePage = new PageImpl<>(movies, customPageable, 1);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(customPageable);
        when(movieRepository.findMoviesByHallId(hallId, customPageable)).thenReturn(moviePage);
        when(movieMapper.mapMovieToMovieResponse(mockMovie)).thenReturn(mockMovieResponse);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getMoviesByHallId(hallId, page, size, sort, type);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        verify(pageableHelper).pageableSort(page, size, sort, type);
    }

    @Test
    @DisplayName("Should handle multiple movies correctly")
    void getMoviesByHallId_WithMultipleMovies_ShouldReturnAllMapped() {
        // Given
        Long hallId = 1L;

        Movie movie1 = Movie.builder().id(1L).title("Movie 1").build();
        Movie movie2 = Movie.builder().id(2L).title("Movie 2").build();
        MovieResponse response1 = MovieResponse.builder().id(1L).title("Movie 1").build();
        MovieResponse response2 = MovieResponse.builder().id(2L).title("Movie 2").build();

        List<Movie> movies = Arrays.asList(movie1, movie2);
        Page<Movie> moviePage = new PageImpl<>(movies, mockPageable, 2);

        when(pageableHelper.pageableSort(0, 10, "title", "ASC")).thenReturn(mockPageable);
        when(movieRepository.findMoviesByHallId(hallId, mockPageable)).thenReturn(moviePage);
        when(movieMapper.mapMovieToMovieResponse(movie1)).thenReturn(response1);
        when(movieMapper.mapMovieToMovieResponse(movie2)).thenReturn(response2);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getMoviesByHallId(hallId, 0, 10, "title", "ASC");

        // Then
        assertThat(result.getObject().getContent()).hasSize(2);
        assertThat(result.getObject().getTotalElements()).isEqualTo(2);
        verify(movieMapper, times(2)).mapMovieToMovieResponse(any(Movie.class));
    }
}