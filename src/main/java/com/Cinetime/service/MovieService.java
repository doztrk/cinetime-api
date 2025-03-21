package com.Cinetime.service;

import com.Cinetime.entity.Hall;
import com.Cinetime.entity.Movie;
import com.Cinetime.entity.PosterImage;
import com.Cinetime.entity.Showtime;
import com.Cinetime.enums.MovieStatus;
import com.Cinetime.exception.ResourceNotFoundException;
import com.Cinetime.helpers.MovieHelper;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.payload.dto.MovieRequest;
import com.Cinetime.payload.mappers.MovieMapper;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.payload.response.BaseUserResponse;
import com.Cinetime.payload.response.ResponseMessage;
import com.Cinetime.repo.HallRepository;
import com.Cinetime.repo.MovieRepository;
import com.Cinetime.repo.ShowtimeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final PageableHelper pageableHelper;
    private final MovieMapper movieMapper;
    private final HallRepository hallRepository;
    private final ShowtimeRepository showtimeRepository;
    private final MovieHelper movieHelper;


    public ResponseEntity<List<Movie>> getComingSoonMovies(int page, int size, String sort, String type) {
        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);

        List<Movie> movies = movieRepository.findByStatus(MovieStatus.COMING_SOON, pageable);

        if (movies.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(movies);
        }
    }

    @Transactional
    public ResponseMessage<Movie> createMovie(MovieRequest movieRequest) {
        // Validate input
        movieHelper.validateMovieRequest(movieRequest);

        Movie newMovie = movieMapper.mapMovieRequestToMovie(movieRequest);

        // Handle Hall relationship
        Hall hallToBeSet = hallRepository.findById(movieRequest.getHallId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.HALL_NOT_FOUND));
        newMovie.getHalls().add(hallToBeSet);
        hallToBeSet.getMovies().add(newMovie); // Update both sides of the relationship

        // Handle Showtime relationship
        Showtime showtimeToBeSet = showtimeRepository.findById(movieRequest.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SHOWTIME_NOT_FOUND));
        newMovie.getShowtimes().add(showtimeToBeSet);
        showtimeToBeSet.setMovie(newMovie); // Update both sides of the relationship

        // Handle poster image
        try {
            PosterImage posterImage = new PosterImage();
            posterImage.setName(movieRequest.getTitle() + "_poster");
            posterImage.setType(movieRequest.getPosterImage().getContentType());
            posterImage.setData(movieRequest.getPosterImage().getBytes());
            newMovie.setPoster(posterImage);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process poster image", e);
        }

        // Save the movie
        Movie savedMovie = movieRepository.save(newMovie);

        return ResponseMessage.<Movie>builder()
                .message(SuccessMessages.MOVIE_CREATE)
                .httpStatus(HttpStatus.CREATED)
                .object(savedMovie)
                .build();
    }
}

