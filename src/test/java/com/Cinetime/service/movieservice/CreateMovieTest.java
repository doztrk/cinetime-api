package com.Cinetime.service.movieservice;

import com.Cinetime.entity.Movie;
import com.Cinetime.enums.MovieStatus;
import com.Cinetime.helpers.MovieHelper;
import com.Cinetime.payload.dto.request.MovieRequest;
import com.Cinetime.payload.dto.response.MovieResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.mappers.MovieMapper;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.repo.MovieRepository;
import com.Cinetime.service.CloudinaryService;
import com.Cinetime.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateMovieTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieHelper movieHelper;

    @Mock
    private MovieMapper movieMapper;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private MovieService movieService;

    private MovieRequest movieRequest;
    private Movie movie;
    private MovieResponse movieResponse;
    private MultipartFile posterImage;

    @BeforeEach
    void setUp() {
        // Setup test data
        posterImage = new MockMultipartFile(
                "poster",
                "test-poster.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        movieRequest = MovieRequest.builder()
                .title("Test Movie")
                .slug("test-movie")
                .summary("A test movie summary")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .duration(120)
                .rating(8.5)
                .director("Test Director")
                .cast(Arrays.asList("Actor 1", "Actor 2"))
                .formats(Arrays.asList("IMAX", "Standard"))
                .genre(Arrays.asList("Action", "Drama"))
                .status(MovieStatus.COMING_SOON)
                .posterImage(posterImage)
                .build();

        movie = Movie.builder()
                .id(1L)
                .title("Test Movie")
                .slug("test-movie")
                .summary("A test movie summary")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .duration(120)
                .rating(8.5)
                .director("Test Director")
                .cast(Arrays.asList("Actor 1", "Actor 2"))
                .formats(Arrays.asList("IMAX", "Standard"))
                .genre(Arrays.asList("Action", "Drama"))
                .status(MovieStatus.COMING_SOON)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        movieResponse = MovieResponse.builder()
                .id(1L)
                .title("Test Movie")
                .slug("test-movie")
                .summary("A test movie summary")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .duration(120)
                .rating(8.5)
                .director("Test Director")
                .cast(Arrays.asList("Actor 1", "Actor 2"))
                .formats(Arrays.asList("IMAX", "Standard"))
                .genre(Arrays.asList("Action", "Drama"))
                .status(MovieStatus.COMING_SOON)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create movie successfully with poster image")
    void shouldCreateMovieSuccessfullyWithPoster() throws IOException {
        // Given
        String expectedImageUrl = "https://cloudinary.com/test-image.jpg";
        Movie savedMovieWithoutPoster = movie.toBuilder().build();
        Movie savedMovieWithPoster = movie.toBuilder().posterUrl(expectedImageUrl).build();

        when(movieMapper.mapMovieRequestToMovie(movieRequest)).thenReturn(movie);
        when(movieRepository.save(any(Movie.class)))
                .thenReturn(savedMovieWithoutPoster)  // First save
                .thenReturn(savedMovieWithPoster);    // Second save with poster URL
        when(cloudinaryService.uploadMoviePoster(eq(posterImage), eq(1L))).thenReturn(expectedImageUrl);
        when(movieMapper.mapMovieToMovieResponse(savedMovieWithPoster)).thenReturn(movieResponse);

        // When
        ResponseMessage<MovieResponse> result = movieService.createMovie(movieRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.MOVIE_CREATE);
        assertThat(result.getObject()).isEqualTo(movieResponse);

        // Verify interactions
        verify(movieHelper).validateMovieRequest(movieRequest);
        verify(cloudinaryService).uploadMoviePoster(eq(posterImage), eq(1L));
        verify(movieRepository, times(2)).save(any(Movie.class)); // Correctly expect 2 saves

        // Verify the first save (without poster)
        ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);
        verify(movieRepository, times(2)).save(movieCaptor.capture());

        Movie firstSave = movieCaptor.getAllValues().get(0);
        Movie secondSave = movieCaptor.getAllValues().get(1);

        assertThat(firstSave.getPosterUrl()).isNull();
        assertThat(secondSave.getPosterUrl()).isEqualTo(expectedImageUrl);
    }

    @Test
    @DisplayName("Should create movie successfully without poster image")
    void shouldCreateMovieSuccessfullyWithoutPoster() {
        // Given
        movieRequest.setPosterImage(null);
        when(movieMapper.mapMovieRequestToMovie(movieRequest)).thenReturn(movie);
        when(movieRepository.save(any(Movie.class))).thenReturn(movie);
        when(movieMapper.mapMovieToMovieResponse(movie)).thenReturn(movieResponse);

        // When
        ResponseMessage<MovieResponse> result = movieService.createMovie(movieRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.MOVIE_CREATE);
        assertThat(result.getObject()).isEqualTo(movieResponse);

        // Verify cloudinary service was never called
        verifyNoInteractions(cloudinaryService);

        // Only one save should happen when no image
        verify(movieRepository, times(1)).save(any(Movie.class));

        ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);
        verify(movieRepository).save(movieCaptor.capture());
        Movie capturedMovie = movieCaptor.getValue();
        assertThat(capturedMovie.getPosterUrl()).isNull();
    }

    @Test
    @DisplayName("Should create movie successfully with empty poster image")
    void shouldCreateMovieSuccessfullyWithEmptyPoster() {
        // Given
        MultipartFile emptyFile = new MockMultipartFile("poster", "", "image/jpeg", new byte[0]);
        movieRequest.setPosterImage(emptyFile);

        when(movieMapper.mapMovieRequestToMovie(movieRequest)).thenReturn(movie);
        when(movieRepository.save(any(Movie.class))).thenReturn(movie);
        when(movieMapper.mapMovieToMovieResponse(movie)).thenReturn(movieResponse);

        // When
        ResponseMessage<MovieResponse> result = movieService.createMovie(movieRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.CREATED);

        // Verify cloudinary service was never called for empty file
        verifyNoInteractions(cloudinaryService);
        verify(movieRepository, times(1)).save(any(Movie.class));
    }

    @Test
    @DisplayName("Should handle validation failure")
    void shouldHandleValidationFailure() {
        // Given
        doThrow(new IllegalArgumentException("Invalid movie data"))
                .when(movieHelper).validateMovieRequest(movieRequest);

        // When & Then
        assertThatThrownBy(() -> movieService.createMovie(movieRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid movie data");

        // Verify no further interactions
        verifyNoInteractions(cloudinaryService);
        verifyNoInteractions(movieRepository);
        verify(movieMapper, never()).mapMovieRequestToMovie(any());
    }

    @Test
    @DisplayName("Should handle image upload failure and return error response")
    void shouldHandleImageUploadFailure() throws IOException {
        // Given
        when(movieMapper.mapMovieRequestToMovie(movieRequest)).thenReturn(movie);
        when(movieRepository.save(any(Movie.class))).thenReturn(movie);
        when(cloudinaryService.uploadMoviePoster(eq(posterImage), eq(1L)))
                .thenThrow(new IOException("Cloudinary upload failed"));

        // When
        ResponseMessage<MovieResponse> result = movieService.createMovie(movieRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getMessage()).contains("Failed to upload movie poster");
        assertThat(result.getMessage()).contains("Cloudinary upload failed");
        assertThat(result.getObject()).isNull();

        // Verify movie was initially saved (transaction will rollback due to @Transactional)
        verify(movieRepository, times(1)).save(any(Movie.class));
        verify(cloudinaryService).uploadMoviePoster(eq(posterImage), eq(1L));

        // Note: In reality, @Transactional should rollback the first save
        // but since this is a unit test with mocks, we can't test actual transaction behavior
    }

    @Test
    @DisplayName("Should handle database save failure")
    void shouldHandleDatabaseSaveFailure() {
        // Given
        movieRequest.setPosterImage(null); // No image to avoid cloudinary calls
        when(movieMapper.mapMovieRequestToMovie(movieRequest)).thenReturn(movie);
        when(movieRepository.save(any(Movie.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When
        ResponseMessage<MovieResponse> result = movieService.createMovie(movieRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getMessage()).contains("Failed to create movie");
        assertThat(result.getMessage()).contains("Database connection failed");
        assertThat(result.getObject()).isNull();
    }

    @Test
    @DisplayName("Should set creation and update timestamps correctly")
    void shouldSetTimestamps() {
        // Given
        movieRequest.setPosterImage(null);
        LocalDateTime testStart = LocalDateTime.now();

        when(movieMapper.mapMovieRequestToMovie(movieRequest)).thenReturn(movie);
        when(movieRepository.save(any(Movie.class))).thenReturn(movie);
        when(movieMapper.mapMovieToMovieResponse(movie)).thenReturn(movieResponse);

        // When
        movieService.createMovie(movieRequest);
        LocalDateTime testEnd = LocalDateTime.now();

        // Then
        ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);
        verify(movieRepository).save(movieCaptor.capture());
        Movie capturedMovie = movieCaptor.getValue();

        assertThat(capturedMovie.getCreatedAt()).isNotNull();
        assertThat(capturedMovie.getUpdatedAt()).isNotNull();

        // Verify timestamps are within test execution timeframe
        assertThat(capturedMovie.getCreatedAt()).isBetween(testStart, testEnd);
        assertThat(capturedMovie.getUpdatedAt()).isBetween(testStart, testEnd);

        // Both timestamps should be very close (but not necessarily identical due to execution time)
        assertThat(capturedMovie.getCreatedAt()).isCloseTo(capturedMovie.getUpdatedAt(), within(100, java.time.temporal.ChronoUnit.MILLIS));
    }

    @Test
    @DisplayName("Should handle successful flow with poster and verify all interactions")
    void shouldHandleCompleteSuccessfulFlow() throws IOException {
        // Given
        String expectedImageUrl = "https://cloudinary.com/test-image.jpg";
        Movie movieWithoutPoster = movie.toBuilder().posterUrl(null).build();
        Movie movieWithPoster = movie.toBuilder().posterUrl(expectedImageUrl).build();

        when(movieMapper.mapMovieRequestToMovie(movieRequest)).thenReturn(movie);
        // Use any() for mocking, then verify with ArgumentCaptor for safety
        when(movieRepository.save(any(Movie.class)))
                .thenReturn(movieWithoutPoster)
                .thenReturn(movieWithPoster);
        when(cloudinaryService.uploadMoviePoster(posterImage, 1L)).thenReturn(expectedImageUrl);
        when(movieMapper.mapMovieToMovieResponse(movieWithPoster)).thenReturn(movieResponse);

        // When
        ResponseMessage<MovieResponse> result = movieService.createMovie(movieRequest);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getObject()).isEqualTo(movieResponse);

        // Verify the complete interaction flow
        var inOrder = inOrder(movieHelper, movieMapper, movieRepository, cloudinaryService);
        inOrder.verify(movieHelper).validateMovieRequest(movieRequest);
        inOrder.verify(movieMapper).mapMovieRequestToMovie(movieRequest);
        inOrder.verify(movieRepository).save(any(Movie.class)); // First save
        inOrder.verify(cloudinaryService).uploadMoviePoster(posterImage, 1L);
        inOrder.verify(movieRepository).save(any(Movie.class)); // Second save
        inOrder.verify(movieMapper).mapMovieToMovieResponse(movieWithPoster);

        // Verify the actual values passed to save() using ArgumentCaptor
        ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);
        verify(movieRepository, times(2)).save(movieCaptor.capture());

        List<Movie> savedMovies = movieCaptor.getAllValues();
        assertThat(savedMovies.get(0).getPosterUrl()).isNull(); // First save should have null poster
        assertThat(savedMovies.get(1).getPosterUrl()).isEqualTo(expectedImageUrl); // Second save should have poster URL
    }
}