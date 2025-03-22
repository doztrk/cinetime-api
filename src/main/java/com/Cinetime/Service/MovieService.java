package com.Cinetime.service;


import com.Cinetime.entity.Movie;
import com.Cinetime.entity.PosterImage;
import com.Cinetime.enums.MovieStatus;
import com.Cinetime.helpers.MovieHelper;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.payload.dto.MovieRequest;
import com.Cinetime.payload.mappers.MovieMapper;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.payload.response.MovieResponse;
import com.Cinetime.payload.response.ResponseMessage;
import com.Cinetime.repo.HallRepository;
import com.Cinetime.repo.MovieRepository;
import com.Cinetime.repo.PosterImageRepository;
import com.Cinetime.repo.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final PageableHelper pageableHelper;
    private final MovieMapper movieMapper;
    private final HallRepository hallRepository;
    private final ShowtimeRepository showtimeRepository;
    private final MovieHelper movieHelper;
    private final PosterImageRepository posterImageRepository;


    //M12
    public MovieResponse updateMovie(Long id, MovieRequest movieRequest) {
        Movie movie = movieRepository.findById(id).orElse(null);
        if (movie == null) {
            return null;
        }
        movie = movieMapper.mapMovieRequestToMovie(movieRequest);
        movie.setId(id);
        movieRepository.save(movie);
        return movieMapper.mapMovieToMovieResponse(movie);
    }

    //M13
    public boolean deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            return false;
        }
        movieRepository.deleteById(id);
        return true;
    }

    public ResponseEntity<List<Movie>> getMovieByHall(int page, int size, String sort, String type, String hall) {
        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);

        List<Movie> movies = movieRepository.findByHalls_NameIgnoreCase(hall,pageable);

        if (movies.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(movies);
        }
    }

    public ResponseEntity<List<Movie>> getInTheatersMovies(int page, int size, String sort, String type) {

        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);

        List<Movie> movies = movieRepository.findByStatus(MovieStatus.IN_THEATERS, pageable);

        if (movies.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(movies);
        }
    }

    public ResponseEntity<List<Movie>> getComingSoonMovies(int page, int size, String sort, String type) {
        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);

        List<Movie> movies = movieRepository.findByStatus(MovieStatus.COMING_SOON, pageable);

        if (movies.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(movies);
        }
    }


    public ResponseMessage<Movie> createMovie(MovieRequest movieRequest) {
        // Validate input
        movieHelper.validateMovieRequest(movieRequest);

        Movie newMovie = movieMapper.mapMovieRequestToMovie(movieRequest);

        // Handle Hall relationship
/*        Hall hallToBeSet = hallRepository.findById(movieRequest.getHallId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.HALL_NOT_FOUND));
        newMovie.getHalls().add(hallToBeSet);
        hallToBeSet.getMovies().add(newMovie); // Update both sides of the relationship

        // Handle Showtime relationship
        Showtime showtimeToBeSet = showtimeRepository.findById(movieRequest.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SHOWTIME_NOT_FOUND));
        newMovie.getShowtimes().add(showtimeToBeSet);
        showtimeToBeSet.setMovie(newMovie); // Update both sides of the relationship*/

        // Handle poster image
        if (movieRequest.getPosterImage() != null && !movieRequest.getPosterImage().isEmpty()) {
            try {
                PosterImage posterImage = new PosterImage();
                posterImage.setName(movieRequest.getPosterImage().getOriginalFilename());
                posterImage.setType(movieRequest.getPosterImage().getContentType());
                posterImage.setData(movieRequest.getPosterImage().getBytes());
                PosterImage posterImageSaved = posterImageRepository.save(posterImage);

                newMovie.setPoster(posterImageSaved);
            } catch (IOException e) {
                return ResponseMessage.<Movie>builder()
                        .message("Failed to process image: " + e.getMessage())
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .build();
            }


        }

        // Save the movie
        newMovie.setCreatedAt(LocalDate.now());
        newMovie.setUpdatedAt(LocalDate.now());
        Movie savedMovie = movieRepository.save(newMovie);

        return ResponseMessage.<Movie>builder()
                .message(SuccessMessages.MOVIE_CREATE)
                .httpStatus(HttpStatus.CREATED)
                .object(savedMovie)
                .build();
    }


}
/*
    // Update a movie
    public MovieResponse updateMovie(Long id, MovieRequest movieRequest) {
        Movie movie = movieRepository.findByid(id).orElse(null);
        if (movie == null) {
            return null;
        }
        movie = movieMapper.toEntity(movieRequest);
        movie.setId(id);
        movieRepository.save(movie);
        return movieMapper.toResponse(movie);
    }

    // Delete a movie
    public boolean deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            return false;
        }
        movieRepository.deleteById(id);
        return true;
    }

 */




