package com.Cinetime.service;


import com.Cinetime.entity.Movie;
import com.Cinetime.entity.PosterImage;
import com.Cinetime.enums.MovieStatus;
import com.Cinetime.exception.ResourceNotFoundException;
import com.Cinetime.helpers.MovieHelper;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.payload.dto.MovieRequest;
import com.Cinetime.payload.mappers.MovieMapper;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.payload.response.MovieResponse;
import com.Cinetime.payload.response.ResponseMessage;
import com.Cinetime.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    //M01 - Get Movies By Page
    public Page<MovieResponse> getMoviesByPage(int page, int size, String sort, String type) {
        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);
        Page<Movie> movies = movieRepository.findAll(pageable);

        // Boş Liste Kontrolü
        if (movies.isEmpty()) {
            throw new ResourceNotFoundException("No movies found in the database.");
        }

        // DTO Mapping
        return movies.map(movieMapper::mapMovieToMovieResponse);
    }

    // M02 - Return Movies Based on Cinema Slug
    public List<MovieResponse> getMoviesByCinemaSlug(String slug) {
        List<Movie> movies = movieRepository.findByCinemaSlug(slug);
        return movies.stream().map(movieMapper::mapMovieToMovieResponse).toList();
    }
}

