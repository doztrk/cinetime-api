package com.Cinetime.service.movieservice;

import com.Cinetime.entity.Hall;
import com.Cinetime.entity.Movie;
import com.Cinetime.entity.Showtime;
import com.Cinetime.enums.MovieStatus;
import com.Cinetime.helpers.MovieHelperUpdate;
import com.Cinetime.payload.dto.request.MovieRequestUpdate;
import com.Cinetime.payload.dto.response.MovieResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.mappers.MovieMapper;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.repo.MovieRepository;
import com.Cinetime.repo.ShowtimeRepository;
import com.Cinetime.service.CloudinaryService;
import com.Cinetime.service.MovieService;
import com.Cinetime.service.ShowtimeService;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateMovieByIdTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieHelperUpdate movieHelperUpdate;

    @Mock
    private MovieMapper movieMapper;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private ShowtimeService showtimeService;

    @Mock
    private ShowtimeRepository showtimeRepository;

    @InjectMocks
    private MovieService movieService;

    @Mock
    private MultipartFile mockFile;

    @Mock
    private Showtime mockShowtime;

    @Mock
    private Hall mockHall;

    private Movie existingMovie;
    private MovieRequestUpdate updateRequest;
    private MovieResponse expectedResponse;

    @BeforeEach
    void setUp() {
        // Setup existing movie
        existingMovie = Movie.builder()
                .id(1L)
                .title("Original Title")
                .slug("original-slug")
                .summary("Original summary")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .duration(120)
                .rating(7.5)
                .director("Original Director")
                .cast(Arrays.asList("Actor 1", "Actor 2"))
                .formats(Arrays.asList("2D", "3D"))
                .genre(Arrays.asList("Action", "Drama"))
                .status(MovieStatus.IN_THEATERS)
                .posterUrl("https://old-image-url.com/image.jpg")
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();

        // Setup update request
        updateRequest = MovieRequestUpdate.builder()
                .title("Updated Title")
                .slug("updated-slug")
                .summary("Updated summary")
                .releaseDate(LocalDate.of(2024, 6, 1))
                .duration(140)
                .rating(8.0)
                .director("Updated Director")
                .cast(Arrays.asList("New Actor 1", "New Actor 2"))
                .formats(Arrays.asList("IMAX", "4DX"))
                .genre(Arrays.asList("Sci-Fi", "Thriller"))
                .status(MovieStatus.COMING_SOON)
                .posterImage(mockFile)
                .showtimeId(100L)
                .build();

        // Setup expected response
        expectedResponse = MovieResponse.builder()
                .id(1L)
                .title("Updated Title")
                .slug("updated-slug")
                .build();
    }

    @Test
    void updateMovie_Success_WithImageUpdate() throws BadRequestException, IOException {
        // Arrange
        when(movieRepository.findById(1L)).thenReturn(Optional.of(existingMovie));
        when(mockFile.isEmpty()).thenReturn(false);
        when(cloudinaryService.updateMoviePoster(mockFile, 1L, "https://old-image-url.com/image.jpg"))
                .thenReturn("https://new-image-url.com/image.jpg");
        when(movieRepository.save(any(Movie.class))).thenReturn(existingMovie);
        when(movieMapper.mapMovieToMovieResponse(existingMovie)).thenReturn(expectedResponse);

        // Setup showtime mocks for this specific test
        when(mockHall.getId()).thenReturn(1L);
        when(mockShowtime.getId()).thenReturn(100L);
        when(mockShowtime.getHall()).thenReturn(mockHall);
        when(mockShowtime.getDate()).thenReturn(LocalDate.now().plusDays(1));
        when(mockShowtime.getStartTime()).thenReturn(LocalTime.of(20, 0));
        when(mockShowtime.getEndTime()).thenReturn(LocalTime.of(22, 0));
        when(showtimeRepository.findById(100L)).thenReturn(Optional.of(mockShowtime));

        // Act
        ResponseMessage<MovieResponse> result = movieService.updateMovie(1L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getHttpStatus());
        assertEquals(SuccessMessages.MOVIE_UPDATE, result.getMessage());
        assertEquals(expectedResponse, result.getObject());

        // Verify interactions
        verify(movieHelperUpdate).validateMovieRequest(updateRequest);
        verify(movieRepository).findById(1L);
        verify(showtimeRepository).findById(100L);
        verify(showtimeService).showtimeUpdateCheck(100L, 1L,
                LocalDate.now().plusDays(1), LocalTime.of(20, 0), LocalTime.of(22, 0));
        verify(cloudinaryService).updateMoviePoster(mockFile, 1L, "https://old-image-url.com/image.jpg");
        verify(movieRepository).save(existingMovie);
        verify(movieMapper).mapMovieToMovieResponse(existingMovie);

        // Verify movie fields were updated
        assertEquals("Updated Title", existingMovie.getTitle());
        assertEquals("Updated summary", existingMovie.getSummary());
        assertEquals("https://new-image-url.com/image.jpg", existingMovie.getPosterUrl());
        assertNotNull(existingMovie.getUpdatedAt());
    }

    @Test
    void updateMovie_Success_WithoutImageUpdate() throws BadRequestException, IOException {
        // Arrange
        updateRequest.setPosterImage(null);
        when(movieRepository.findById(1L)).thenReturn(Optional.of(existingMovie));
        when(movieRepository.save(any(Movie.class))).thenReturn(existingMovie);
        when(movieMapper.mapMovieToMovieResponse(existingMovie)).thenReturn(expectedResponse);

        // Setup showtime mocks for this specific test
        when(mockHall.getId()).thenReturn(1L);
        when(mockShowtime.getId()).thenReturn(100L);
        when(mockShowtime.getHall()).thenReturn(mockHall);
        when(mockShowtime.getDate()).thenReturn(LocalDate.now().plusDays(1));
        when(mockShowtime.getStartTime()).thenReturn(LocalTime.of(20, 0));
        when(mockShowtime.getEndTime()).thenReturn(LocalTime.of(22, 0));
        when(showtimeRepository.findById(100L)).thenReturn(Optional.of(mockShowtime));

        // Act
        ResponseMessage<MovieResponse> result = movieService.updateMovie(1L, updateRequest);

        // Assert
        assertEquals(HttpStatus.OK, result.getHttpStatus());
        assertEquals(SuccessMessages.MOVIE_UPDATE, result.getMessage());

        // Verify cloudinary service was not called
        verify(cloudinaryService, never()).updateMoviePoster(any(), any(), any());

        // Original poster URL should remain unchanged
        assertEquals("https://old-image-url.com/image.jpg", existingMovie.getPosterUrl());
    }

    @Test
    void updateMovie_Success_WithEmptyImage() throws BadRequestException, IOException {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(true);
        when(movieRepository.findById(1L)).thenReturn(Optional.of(existingMovie));
        when(movieRepository.save(any(Movie.class))).thenReturn(existingMovie);
        when(movieMapper.mapMovieToMovieResponse(existingMovie)).thenReturn(expectedResponse);
        updateRequest.setShowtimeId(null);
        // Act
        ResponseMessage<MovieResponse> result = movieService.updateMovie(1L, updateRequest);

        // Assert
        assertEquals(HttpStatus.OK, result.getHttpStatus());
        verify(cloudinaryService, never()).updateMoviePoster(any(), any(), any());
    }

    @Test
    void updateMovie_MovieNotFound() throws BadRequestException, IOException {
        // Arrange
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        ResponseMessage<MovieResponse> result = movieService.updateMovie(1L, updateRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getHttpStatus());
        assertEquals(ErrorMessages.MOVIE_NOT_FOUND, result.getMessage());
        assertNull(result.getObject());

        // Verify no further processing occurred
        verify(cloudinaryService, never()).updateMoviePoster(any(), any(), any());
        verify(movieRepository, never()).save(any());
    }

    @Test
    void updateMovie_ValidationFailure() throws BadRequestException {
        // Arrange
        doThrow(new IllegalArgumentException("Validation failed"))
                .when(movieHelperUpdate).validateMovieRequest(any(MovieRequestUpdate.class));

        // Act
        ResponseMessage<MovieResponse> result = movieService.updateMovie(1L, updateRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getHttpStatus());
        assertTrue(result.getMessage().contains("Validation failed"));
        assertNull(result.getObject());

        // Verify no repository calls were made
        verify(movieRepository, never()).findById(any());
        verify(movieRepository, never()).save(any());
    }

    @Test
    void updateMovie_ImageUploadFailure() throws BadRequestException, IOException {
        // Arrange
        when(movieRepository.findById(1L)).thenReturn(Optional.of(existingMovie));
        when(mockFile.isEmpty()).thenReturn(false);
        when(cloudinaryService.updateMoviePoster(mockFile, 1L, "https://old-image-url.com/image.jpg"))
                .thenThrow(new IOException("Upload failed"));

        updateRequest.setShowtimeId(null);

        // Act
        ResponseMessage<MovieResponse> result = movieService.updateMovie(1L, updateRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getHttpStatus());
        assertTrue(result.getMessage().contains("Failed to update movie poster"));
        assertNull(result.getObject());

        // Verify movie was not saved after image upload failure
        verify(movieRepository, never()).save(any());
    }

    @Test
    void updateMovie_ShowtimeNotFound() throws BadRequestException, IOException {
        // Arrange
        when(movieRepository.findById(1L)).thenReturn(Optional.of(existingMovie));
        when(showtimeRepository.findById(100L)).thenReturn(Optional.empty());
        updateRequest.setPosterImage(null); // Remove image to focus on showtime error

        // Act
        ResponseMessage<MovieResponse> result = movieService.updateMovie(1L, updateRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getHttpStatus());
        assertEquals(ErrorMessages.SHOWTIME_NOT_FOUND, result.getMessage());
        assertNull(result.getObject());

        // Verify no further processing occurred
        verify(movieRepository, never()).save(any());
    }

    @Test
    void updateMovie_ShowtimeUpdateFailure() throws BadRequestException, IOException {
        // Arrange
        updateRequest.setPosterImage(null); // Remove image to focus on showtime error
        when(movieRepository.findById(1L)).thenReturn(Optional.of(existingMovie));

        // Setup showtime mocks
        when(mockHall.getId()).thenReturn(1L);
        when(mockShowtime.getId()).thenReturn(100L);
        when(mockShowtime.getHall()).thenReturn(mockHall);
        when(mockShowtime.getDate()).thenReturn(LocalDate.now().plusDays(1));
        when(mockShowtime.getStartTime()).thenReturn(LocalTime.of(20, 0));
        when(mockShowtime.getEndTime()).thenReturn(LocalTime.of(22, 0));
        when(showtimeRepository.findById(100L)).thenReturn(Optional.of(mockShowtime));

        // Mock the showtimeService to throw BadRequestException
        doThrow(new BadRequestException("Showtime conflict"))
                .when(showtimeService).showtimeUpdateCheck(100L, 1L,
                        LocalDate.now().plusDays(1), LocalTime.of(20, 0), LocalTime.of(22, 0));

        // Act
        ResponseMessage<MovieResponse> result = movieService.updateMovie(1L, updateRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getHttpStatus());
        assertTrue(result.getMessage().contains("Showtime conflict"));
        assertNull(result.getObject());
    }

    @Test
    void updateMovie_DatabaseSaveFailure() throws BadRequestException, IOException {
        // Arrange
        updateRequest.setPosterImage(null);
        when(movieRepository.findById(1L)).thenReturn(Optional.of(existingMovie));
        when(movieRepository.save(any(Movie.class)))
                .thenThrow(new RuntimeException("Database error"));

        updateRequest.setShowtimeId(null);

        // Act
        ResponseMessage<MovieResponse> result = movieService.updateMovie(1L, updateRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getHttpStatus());
        assertTrue(result.getMessage().contains("Failed to update movie"));
        assertNull(result.getObject());
    }

    @Test
    void updateMovie_Success_WithoutShowtimeId() throws BadRequestException, IOException {
        // Arrange
        updateRequest.setShowtimeId(null);
        updateRequest.setPosterImage(null);
        when(movieRepository.findById(1L)).thenReturn(Optional.of(existingMovie));
        when(movieRepository.save(any(Movie.class))).thenReturn(existingMovie);
        when(movieMapper.mapMovieToMovieResponse(existingMovie)).thenReturn(expectedResponse);

        // Act
        ResponseMessage<MovieResponse> result = movieService.updateMovie(1L, updateRequest);

        // Assert
        assertEquals(HttpStatus.OK, result.getHttpStatus());
        assertEquals(SuccessMessages.MOVIE_UPDATE, result.getMessage());

        // Verify showtime-related services were not called
        verify(showtimeRepository, never()).findById(any());
        verify(showtimeService, never()).showtimeUpdateCheck(any(), any(), any(), any(), any());
    }

    @Test
    void updateMovie_Success_CompleteFlow() throws BadRequestException, IOException {
        // Arrange - Test all components together
        when(movieRepository.findById(1L)).thenReturn(Optional.of(existingMovie));
        when(mockFile.isEmpty()).thenReturn(false);
        when(cloudinaryService.updateMoviePoster(mockFile, 1L, "https://old-image-url.com/image.jpg"))
                .thenReturn("https://new-image-url.com/image.jpg");
        when(movieRepository.save(any(Movie.class))).thenReturn(existingMovie);
        when(movieMapper.mapMovieToMovieResponse(existingMovie)).thenReturn(expectedResponse);

        // Setup showtime mocks for this specific test
        when(mockHall.getId()).thenReturn(1L);
        when(mockShowtime.getId()).thenReturn(100L);
        when(mockShowtime.getHall()).thenReturn(mockHall);
        when(mockShowtime.getDate()).thenReturn(LocalDate.now().plusDays(1));
        when(mockShowtime.getStartTime()).thenReturn(LocalTime.of(20, 0));
        when(mockShowtime.getEndTime()).thenReturn(LocalTime.of(22, 0));
        when(showtimeRepository.findById(100L)).thenReturn(Optional.of(mockShowtime));

        // Act
        ResponseMessage<MovieResponse> result = movieService.updateMovie(1L, updateRequest);

        // Assert
        assertEquals(HttpStatus.OK, result.getHttpStatus());
        assertEquals(SuccessMessages.MOVIE_UPDATE, result.getMessage());
        assertEquals(expectedResponse, result.getObject());

        // Verify all components were called in correct order
        verify(movieHelperUpdate).validateMovieRequest(updateRequest);
        verify(movieRepository).findById(1L);
        verify(showtimeRepository).findById(100L);
        verify(showtimeService).showtimeUpdateCheck(100L, 1L,
                LocalDate.now().plusDays(1), LocalTime.of(20, 0), LocalTime.of(22, 0));
        verify(cloudinaryService).updateMoviePoster(mockFile, 1L, "https://old-image-url.com/image.jpg");
        verify(movieRepository).save(existingMovie);
        verify(movieMapper).mapMovieToMovieResponse(existingMovie);
    }
}