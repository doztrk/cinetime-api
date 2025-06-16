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
@DisplayName("MovieService - getMovieByHall Tests")
class GetMovieByHallTest {

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
                .title("Dune: Part Two")
                .slug("dune-part-two")
                .summary("Epic sci-fi sequel")
                .releaseDate(LocalDate.of(2024, 3, 1))
                .duration(165)
                .rating(8.5)
                .director("Denis Villeneuve")
                .cast(Arrays.asList("Timothée Chalamet", "Zendaya"))
                .formats(Arrays.asList("IMAX", "Standard"))
                .genre(Arrays.asList("Sci-Fi", "Adventure"))
                .status(MovieStatus.IN_THEATERS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testMovie2 = Movie.builder()
                .id(2L)
                .title("Oppenheimer")
                .slug("oppenheimer")
                .summary("Biographical thriller")
                .releaseDate(LocalDate.of(2023, 7, 21))
                .duration(180)
                .rating(8.3)
                .director("Christopher Nolan")
                .cast(Arrays.asList("Cillian Murphy", "Emily Blunt"))
                .formats(Arrays.asList("IMAX", "70mm"))
                .genre(Arrays.asList("Biography", "Drama"))
                .status(MovieStatus.IN_THEATERS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testMovieResponse1 = MovieResponse.builder()
                .id(1L)
                .title("Dune: Part Two")
                .slug("dune-part-two")
                .summary("Epic sci-fi sequel")
                .releaseDate(LocalDate.of(2024, 3, 1))
                .duration(165)
                .rating(8.5)
                .director("Denis Villeneuve")
                .cast(Arrays.asList("Timothée Chalamet", "Zendaya"))
                .formats(Arrays.asList("IMAX", "Standard"))
                .genre(Arrays.asList("Sci-Fi", "Adventure"))
                .status(MovieStatus.IN_THEATERS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testMovieResponse2 = MovieResponse.builder()
                .id(2L)
                .title("Oppenheimer")
                .slug("oppenheimer")
                .summary("Biographical thriller")
                .releaseDate(LocalDate.of(2023, 7, 21))
                .duration(180)
                .rating(8.3)
                .director("Christopher Nolan")
                .cast(Arrays.asList("Cillian Murphy", "Emily Blunt"))
                .formats(Arrays.asList("IMAX", "70mm"))
                .genre(Arrays.asList("Biography", "Drama"))
                .status(MovieStatus.IN_THEATERS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testPageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("Should return movies successfully when hall has movies")
    void getMovieByHall_WhenHallHasMovies_ShouldReturnMoviesSuccessfully() {
        // Given
        String hallName = "IMAX";
        int page = 0, size = 10;
        String sort = "title", type = "asc";

        List<Movie> movieList = Arrays.asList(testMovie1, testMovie2);
        Page<Movie> moviePage = new PageImpl<>(movieList, testPageable, movieList.size());

        List<MovieResponse> movieResponseList = Arrays.asList(testMovieResponse1, testMovieResponse2);
        Page<MovieResponse> expectedResponsePage = new PageImpl<>(movieResponseList, testPageable, movieResponseList.size());

        // Mock dependencies
        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(movieRepository.findMoviesByHallName(hallName, testPageable)).thenReturn(moviePage);
        when(movieMapper.mapMoviePageToMovieResponse(moviePage)).thenReturn(expectedResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getMovieByHall(page, size, sort, type, hallName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo("Movies found successfully");
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getContent()).hasSize(2);
        assertThat(result.getObject().getContent().get(0).getTitle()).isEqualTo("Dune: Part Two");
        assertThat(result.getObject().getContent().get(1).getTitle()).isEqualTo("Oppenheimer");

        // Verify interactions
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(movieRepository).findMoviesByHallName(hallName, testPageable);
        verify(movieMapper).mapMoviePageToMovieResponse(moviePage);
        verifyNoMoreInteractions(pageableHelper, movieRepository, movieMapper);
    }

    @Test
    @DisplayName("Should return empty page when hall has no movies")
    void getMovieByHall_WhenHallHasNoMovies_ShouldReturnEmptyPage() {
        // Given
        String hallName = "VIP";
        int page = 0, size = 10;
        String sort = "title", type = "asc";

        Page<Movie> emptyMoviePage = new PageImpl<>(Collections.emptyList(), testPageable, 0);
        Page<MovieResponse> emptyResponsePage = new PageImpl<>(Collections.emptyList(), testPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(movieRepository.findMoviesByHallName(hallName, testPageable)).thenReturn(emptyMoviePage);
        when(movieMapper.mapMoviePageToMovieResponse(emptyMoviePage)).thenReturn(emptyResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getMovieByHall(page, size, sort, type, hallName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo("Movies found successfully");
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getContent()).isEmpty();
        assertThat(result.getObject().getTotalElements()).isEqualTo(0);

        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(movieRepository).findMoviesByHallName(hallName, testPageable);
        verify(movieMapper).mapMoviePageToMovieResponse(emptyMoviePage);
    }

    @Test
    @DisplayName("Should handle different pagination parameters correctly")
    void getMovieByHall_WithDifferentPaginationParams_ShouldHandleCorrectly() {
        // Given
        String hallName = "4DX";
        int page = 2, size = 5;
        String sort = "releaseDate", type = "desc";

        Pageable customPageable = PageRequest.of(page, size);
        List<Movie> movieList = Arrays.asList(testMovie1);
        Page<Movie> moviePage = new PageImpl<>(movieList, customPageable, 1);
        Page<MovieResponse> responsePage = new PageImpl<>(Arrays.asList(testMovieResponse1), customPageable, 1);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(customPageable);
        when(movieRepository.findMoviesByHallName(hallName, customPageable)).thenReturn(moviePage);
        when(movieMapper.mapMoviePageToMovieResponse(moviePage)).thenReturn(responsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getMovieByHall(page, size, sort, type, hallName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject().getContent()).hasSize(1);

        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(movieRepository).findMoviesByHallName(hallName, customPageable);
        verify(movieMapper).mapMoviePageToMovieResponse(moviePage);
    }

    @Test
    @DisplayName("Should handle null hall name gracefully")
    void getMovieByHall_WithNullHallName_ShouldPassToRepository() {
        // Given
        String hallName = null;
        int page = 0, size = 10;
        String sort = "title", type = "asc";

        Page<Movie> emptyPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);
        Page<MovieResponse> emptyResponsePage = new PageImpl<>(Collections.emptyList(), testPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(movieRepository.findMoviesByHallName(hallName, testPageable)).thenReturn(emptyPage);
        when(movieMapper.mapMoviePageToMovieResponse(emptyPage)).thenReturn(emptyResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getMovieByHall(page, size, sort, type, hallName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);

        // Verify that null is passed to repository (repository should handle null)
        verify(movieRepository).findMoviesByHallName(null, testPageable);
    }

    @Test
    @DisplayName("Should handle empty hall name gracefully")
    void getMovieByHall_WithEmptyHallName_ShouldPassToRepository() {
        // Given
        String hallName = "";
        int page = 0, size = 10;
        String sort = "title", type = "asc";

        Page<Movie> emptyPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);
        Page<MovieResponse> emptyResponsePage = new PageImpl<>(Collections.emptyList(), testPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(movieRepository.findMoviesByHallName(hallName, testPageable)).thenReturn(emptyPage);
        when(movieMapper.mapMoviePageToMovieResponse(emptyPage)).thenReturn(emptyResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result = movieService.getMovieByHall(page, size, sort, type, hallName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);

        verify(movieRepository).findMoviesByHallName("", testPageable);
    }

    @Test
    @DisplayName("Should verify correct method call sequence")
    void getMovieByHall_ShouldCallMethodsInCorrectSequence() {
        // Given
        String hallName = "IMAX";
        int page = 0, size = 10;
        String sort = "title", type = "asc";

        Page<Movie> moviePage = new PageImpl<>(Arrays.asList(testMovie1), testPageable, 1);
        Page<MovieResponse> responsePage = new PageImpl<>(Arrays.asList(testMovieResponse1), testPageable, 1);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(movieRepository.findMoviesByHallName(hallName, testPageable)).thenReturn(moviePage);
        when(movieMapper.mapMoviePageToMovieResponse(moviePage)).thenReturn(responsePage);

        // When
        movieService.getMovieByHall(page, size, sort, type, hallName);

        // Then - Verify call order using InOrder
        var inOrder = inOrder(pageableHelper, movieRepository, movieMapper);
        inOrder.verify(pageableHelper).pageableSort(page, size, sort, type);
        inOrder.verify(movieRepository).findMoviesByHallName(hallName, testPageable);
        inOrder.verify(movieMapper).mapMoviePageToMovieResponse(moviePage);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Should handle case-sensitive hall names")
    void getMovieByHall_WithCaseSensitiveHallNames_ShouldPassExactValue() {
        // Given
        String hallName = "iMAX"; // Mixed case
        int page = 0, size = 10;
        String sort = "title", type = "asc";

        Page<Movie> moviePage = new PageImpl<>(Collections.emptyList(), testPageable, 0);
        Page<MovieResponse> responsePage = new PageImpl<>(Collections.emptyList(), testPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(movieRepository.findMoviesByHallName(hallName, testPageable)).thenReturn(moviePage);
        when(movieMapper.mapMoviePageToMovieResponse(moviePage)).thenReturn(responsePage);

        // When
        movieService.getMovieByHall(page, size, sort, type, hallName);

        // Then
        verify(movieRepository).findMoviesByHallName(eq("iMAX"), any(Pageable.class));
    }
}