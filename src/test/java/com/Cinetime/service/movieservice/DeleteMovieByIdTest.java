package com.Cinetime.service.movieservice;

import com.Cinetime.entity.Movie;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.repo.MovieRepository;
import com.Cinetime.service.CloudinaryService;
import com.Cinetime.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteMovieByIdTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private MovieService movieService;

    private Movie testMovie;
    private static final Long MOVIE_ID = 1L;
    private static final String POSTER_URL = "https://cloudinary.com/test-poster.jpg";

    @BeforeEach
    void setUp() {
        testMovie = Movie.builder()
                .id(MOVIE_ID)
                .title("Test Movie")
                .posterUrl(POSTER_URL)
                .build();
    }

    @Test
    void deleteMovieById_WhenMovieNotFound_ShouldReturnNotFound() {
        // Given
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.empty());

        // When
        ResponseMessage<?> response = movieService.deleteMovieById(MOVIE_ID);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getHttpStatus());
        assertEquals(ErrorMessages.MOVIE_NOT_FOUND, response.getMessage());
        verify(movieRepository, never()).deleteById(any());
        verify(cloudinaryService, never()).deleteMoviePoster(any());
    }

    @Test
    void deleteMovieById_WithPosterUrl_ShouldDeleteBothImageAndMovie() {
        // Given
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(testMovie));
        when(cloudinaryService.deleteMoviePoster(POSTER_URL)).thenReturn(true);

        // When
        ResponseMessage<?> response = movieService.deleteMovieById(MOVIE_ID);

        // Then
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals(SuccessMessages.MOVIE_DELETE, response.getMessage());
        verify(cloudinaryService).deleteMoviePoster(POSTER_URL);
        verify(movieRepository).deleteById(MOVIE_ID);
    }

    @Test
    void deleteMovieById_WithoutPosterUrl_ShouldDeleteOnlyMovie() {
        // Given
        testMovie.setPosterUrl(null);
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(testMovie));

        // When
        ResponseMessage<?> response = movieService.deleteMovieById(MOVIE_ID);

        // Then
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals(SuccessMessages.MOVIE_DELETE, response.getMessage());
        verify(cloudinaryService, never()).deleteMoviePoster(any());
        verify(movieRepository).deleteById(MOVIE_ID);
    }

    @Test
    void deleteMovieById_WithEmptyPosterUrl_ShouldDeleteOnlyMovie() {
        // Given
        testMovie.setPosterUrl("");
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(testMovie));

        // When
        ResponseMessage<?> response = movieService.deleteMovieById(MOVIE_ID);

        // Then
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        verify(cloudinaryService, never()).deleteMoviePoster(any());
        verify(movieRepository).deleteById(MOVIE_ID);
    }

    @Test
    void deleteMovieById_WhenImageDeletionFails_ShouldContinueWithMovieDeletion() {
        // Given
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(testMovie));
        when(cloudinaryService.deleteMoviePoster(POSTER_URL)).thenReturn(false);

        // When
        ResponseMessage<?> response = movieService.deleteMovieById(MOVIE_ID);

        // Then
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals(SuccessMessages.MOVIE_DELETE, response.getMessage());
        verify(cloudinaryService).deleteMoviePoster(POSTER_URL);
        verify(movieRepository).deleteById(MOVIE_ID);
    }

    @Test
    void deleteMovieById_WhenDatabaseDeletionFails_ShouldReturnInternalServerError() {
        // Given
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(testMovie));
        when(cloudinaryService.deleteMoviePoster(POSTER_URL)).thenReturn(true);
        doThrow(new DataAccessException("Database error") {}).when(movieRepository).deleteById(MOVIE_ID);

        // When
        ResponseMessage<?> response = movieService.deleteMovieById(MOVIE_ID);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getHttpStatus());
        assertTrue(response.getMessage().contains("Failed to delete movie"));
        verify(cloudinaryService).deleteMoviePoster(POSTER_URL);
        verify(movieRepository).deleteById(MOVIE_ID);
    }

    @Test
    void deleteMovieById_WhenImageDeletionThrowsException_ShouldReturnInternalServerError() {
        // Given
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(testMovie));
        when(cloudinaryService.deleteMoviePoster(POSTER_URL))
                .thenThrow(new RuntimeException("Cloudinary service unavailable"));

        // When
        ResponseMessage<?> response = movieService.deleteMovieById(MOVIE_ID);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getHttpStatus());
        assertTrue(response.getMessage().contains("Failed to delete movie"));
        verify(cloudinaryService).deleteMoviePoster(POSTER_URL);
        // Database deletion should not be called when image deletion throws exception
        verify(movieRepository, never()).deleteById(MOVIE_ID);
    }

    @Test
    void deleteMovieById_VerifyTransactionalBehavior() {
        // This test shows that your method needs better transaction handling
        // Given
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(testMovie));
        when(cloudinaryService.deleteMoviePoster(POSTER_URL)).thenReturn(true);

        // When
        movieService.deleteMovieById(MOVIE_ID);

        // Then - verify order of operations
        var inOrder = inOrder(cloudinaryService, movieRepository);
        inOrder.verify(cloudinaryService).deleteMoviePoster(POSTER_URL);
        inOrder.verify(movieRepository).deleteById(MOVIE_ID);
    }
}