package com.Cinetime.service;

import com.Cinetime.entity.Showtime;
import com.Cinetime.helpers.MovieHelperUpdate;
import com.Cinetime.payload.dto.request.MovieRequestUpdate;
import com.Cinetime.payload.dto.response.MovieResponse;
import com.Cinetime.payload.dto.response.MovieResponseCinema;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Cinetime.entity.Movie;
import com.Cinetime.enums.MovieStatus;
import com.Cinetime.exception.ResourceNotFoundException;
import com.Cinetime.helpers.MovieHelper;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.payload.dto.request.MovieRequest;
import com.Cinetime.payload.mappers.MovieMapper;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.repo.HallRepository;
import com.Cinetime.repo.MovieRepository;
import com.Cinetime.repo.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    private final MovieRepository movieRepository;
    private final PageableHelper pageableHelper;
    private final MovieMapper movieMapper;
    private final HallRepository hallRepository;
    private final ShowtimeRepository showtimeRepository;
    private final MovieHelper movieHelper;
    private final ShowtimeService showtimeService;
    private final MovieHelperUpdate movieHelperUpdate;
    private final CloudinaryService cloudinaryService;

    public ResponseMessage<Page<MovieResponse>> getMovieByHall(int page, int size, String sort, String type, String hallName) {
        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);

        Page<Movie> pageResult = movieRepository.findMoviesByHallName(hallName, pageable);

        return ResponseMessage.<Page<MovieResponse>>builder()
                .message("Movies found successfully")
                .httpStatus(HttpStatus.OK)
                .object(movieMapper.mapMoviePageToMovieResponse(pageResult))
                .build();
    }

    public ResponseMessage<Page<MovieResponse>> getInTheatersMovies(int page, int size, String sort, String type) {

        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);

        Page<Movie> movies = movieRepository.findByStatus(MovieStatus.IN_THEATERS, pageable);

        if (movies.isEmpty()) {
            return ResponseMessage.<Page<MovieResponse>>builder()
                    .message(ErrorMessages.MOVIE_NOT_FOUND)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        return ResponseMessage.<Page<MovieResponse>>builder()
                .message("Movies found successfully")
                .httpStatus(HttpStatus.OK)
                .object(movieMapper.mapMoviePageToMovieResponse(movies))
                .build();
    }

    public ResponseMessage<Page<MovieResponse>> getComingSoonMovies(int page, int size, String sort, String type) {
        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);

        Page<Movie> movies = movieRepository.findByStatus(MovieStatus.COMING_SOON, pageable);

        if (movies.isEmpty()) {
            return ResponseMessage.<Page<MovieResponse>>builder()
                    .message(ErrorMessages.MOVIE_NOT_FOUND)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        return ResponseMessage.<Page<MovieResponse>>builder()
                .message("Movies found successfully")
                .httpStatus(HttpStatus.OK)
                .object(movieMapper.mapMoviePageToMovieResponse(movies))
                .build();
    }

    @Transactional
    public ResponseMessage<MovieResponse> createMovie(MovieRequest movieRequest) {
        // Validate input first
        movieHelper.validateMovieRequest(movieRequest);

        // Map to entity
        Movie newMovie = movieMapper.mapMovieRequestToMovie(movieRequest);
        newMovie.setCreatedAt(LocalDateTime.now());
        newMovie.setUpdatedAt(LocalDateTime.now());

        try {
            // Save movie first to get the ID (needed for image naming)
            Movie savedMovie = movieRepository.save(newMovie);

            // Handle poster image upload if provided
            if (movieRequest.getPosterImage() != null && !movieRequest.getPosterImage().isEmpty()) {
                try {
                    // Upload to cloud storage
                    String imageUrl = cloudinaryService.uploadMoviePoster(
                            movieRequest.getPosterImage(),
                            savedMovie.getId()
                    );

                    // Update movie with image URL
                    savedMovie.setPosterUrl(imageUrl);
                    savedMovie = movieRepository.save(savedMovie);

                    log.info("Movie created successfully with poster: ID={}, URL={}",
                            savedMovie.getId(), imageUrl);

                } catch (IOException e) {
                    // CRITICAL: If image upload fails, we need to clean up
                    log.error("Image upload failed for movie ID: {}", savedMovie.getId(), e);

                    // Delete the movie record since image upload failed
                    movieRepository.deleteById(savedMovie.getId());

                    return ResponseMessage.<MovieResponse>builder()
                            .message("Failed to upload movie poster: " + e.getMessage())
                            .httpStatus(HttpStatus.BAD_REQUEST)
                            .build();
                }
            }

            return ResponseMessage.<MovieResponse>builder()
                    .message(SuccessMessages.MOVIE_CREATE)
                    .httpStatus(HttpStatus.CREATED)
                    .object(movieMapper.mapMovieToMovieResponse(savedMovie))
                    .build();

        } catch (Exception e) {
            log.error("Failed to create movie", e);
            return ResponseMessage.<MovieResponse>builder()
                    .message("Failed to create movie: " + e.getMessage())
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    //M01 - Get Movies By Query
    public ResponseMessage<Page<MovieResponse>> getMoviesByQuery(String q, int page, int size, String sort, String type) {
        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);


        Page<Movie> movies = movieRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(q, q, pageable);


        return ResponseMessage.<Page<MovieResponse>>builder()
                .message("Movies found successfully")
                .httpStatus(HttpStatus.OK)
                .object(movieMapper.mapMoviePageToMovieResponse(movies))
                .build();
    }


    // M02 - Return Movies Based on Cinema Slug
    public ResponseMessage<List<MovieResponseCinema>> getMoviesByCinemaSlug(String cinemaSlug, int page, int size, String sort, String type) {
        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);

        List<MovieResponseCinema> moviePage = movieRepository.findMoviesByCinemaSlug(cinemaSlug);

        if (moviePage.isEmpty()) {
            return ResponseMessage.<List<MovieResponseCinema>>builder()
                    .message(ErrorMessages.MOVIE_NOT_FOUND)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        return ResponseMessage.<List<MovieResponseCinema>>builder()
                .message("Movies found successfully")
                .httpStatus(HttpStatus.OK)
                .object(moviePage)
                .build();
    }


    public ResponseMessage<MovieResponse> getMoviesById(Long movieId) {

        Movie movie = isMovieExist(movieId);

        return ResponseMessage.<MovieResponse>builder()
                .message("Movies found successfully")
                .httpStatus(HttpStatus.OK)
                .object(movieMapper.mapMovieToMovieResponse(movie))
                .build();
    }

    private Movie isMovieExist(Long id) {
        return movieRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(String.format(ErrorMessages.MOVIE_NOT_FOUND))
        );
    }

    @Transactional
    public ResponseMessage<MovieResponse> updateMovie(Long movieId, MovieRequestUpdate movieRequest)
            throws BadRequestException {

        movieHelperUpdate.validateMovieRequest(movieRequest);

        Movie existingMovie = isMovieExist(movieId);
        String oldImageUrl = existingMovie.getPosterUrl(); // Store for cleanup

        try {
            // Update basic movie fields
            updateMovieFields(existingMovie, movieRequest);

            // Handle Showtime relationship if provided
            if (movieRequest.getShowtimeId() != null) {
                handleShowtimeUpdate(existingMovie, movieRequest.getShowtimeId());
            }

            // Handle poster image update
            if (movieRequest.getPosterImage() != null && !movieRequest.getPosterImage().isEmpty()) {
                try {
                    String newImageUrl = cloudinaryService.updateMoviePoster(
                            movieRequest.getPosterImage(),
                            movieId,
                            oldImageUrl
                    );
                    existingMovie.setPosterUrl(newImageUrl);

                    log.info("Movie poster updated successfully: ID={}, New URL={}",
                            movieId, newImageUrl);

                } catch (IOException e) {
                    log.error("Failed to update movie poster for ID: {}", movieId, e);

                    return ResponseMessage.<MovieResponse>builder()
                            .message("Failed to update movie poster: " + e.getMessage())
                            .httpStatus(HttpStatus.BAD_REQUEST)
                            .build();
                }
            }

            existingMovie.setUpdatedAt(LocalDateTime.now());
            Movie updatedMovie = movieRepository.save(existingMovie);

            return ResponseMessage.<MovieResponse>builder()
                    .message(SuccessMessages.MOVIE_UPDATE)
                    .httpStatus(HttpStatus.OK)
                    .object(movieMapper.mapMovieToMovieResponse(updatedMovie))
                    .build();

        } catch (Exception e) {
            log.error("Failed to update movie ID: {}", movieId, e);
            return ResponseMessage.<MovieResponse>builder()
                    .message("Failed to update movie: " + e.getMessage())
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @Transactional
    public ResponseMessage<?> deleteMovieById(Long movieId) {
        Movie movie = isMovieExist(movieId);

        try {
            // Delete poster from cloud storage first
            if (movie.getPosterUrl() != null && !movie.getPosterUrl().isEmpty()) {
                boolean deleted = cloudinaryService.deleteMoviePoster(movie.getPosterUrl());
                if (!deleted) {
                    log.warn("Failed to delete movie poster from cloud storage: {}",
                            movie.getPosterUrl());
                    // Continue with movie deletion even if image deletion fails
                }
            }

            // Delete movie from database
            movieRepository.deleteById(movieId);

            log.info("Movie deleted successfully: ID={}", movieId);

            return ResponseMessage.builder()
                    .message(SuccessMessages.MOVIE_DELETE)
                    .httpStatus(HttpStatus.OK)
                    .build();

        } catch (Exception e) {
            log.error("Failed to delete movie ID: {}", movieId, e);
            return ResponseMessage.builder()
                    .message("Failed to delete movie: " + e.getMessage())
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    public ResponseMessage<Page<MovieResponse>> getAllMovies(int page, int size, String sort, String type) {

        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);

        Page<Movie> moviePage = movieRepository.findAll(pageable);

        if (moviePage.isEmpty()) {
            return ResponseMessage.<Page<MovieResponse>>builder()
                    .message(ErrorMessages.MOVIES_NOT_FOUND)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        return ResponseMessage.<Page<MovieResponse>>builder()
                .message("Movies found successfully")
                .object(moviePage.map(movieMapper::mapMovieToMovieResponse))
                .httpStatus(HttpStatus.OK)
                .build();

    }


    public ResponseMessage<Page<MovieResponse>> getMoviesByHallId(Long hallId, int page, int size, String sort, String type) {

        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);

        Page<Movie> moviePage = movieRepository.findMoviesByHallId(hallId, pageable);

        if (moviePage.isEmpty()) {
            return ResponseMessage.<Page<MovieResponse>>builder()
                    .message(ErrorMessages.MOVIES_NOT_FOUND)
                    .httpStatus(HttpStatus.NO_CONTENT)
                    .build();
        }


        return ResponseMessage.<Page<MovieResponse>>builder()
                .message("Movies found successfully")
                .object(moviePage.map(movieMapper::mapMovieToMovieResponse))
                .httpStatus(HttpStatus.OK)
                .build();
    }

    private void updateMovieFields(Movie movie, MovieRequestUpdate request) {
        movie.setTitle(request.getTitle());
        movie.setSlug(request.getSlug());
        movie.setSummary(request.getSummary());
        movie.setDuration(request.getDuration());
        movie.setRating(request.getRating());
        movie.setDirector(request.getDirector());
        movie.setCast(request.getCast());
        movie.setGenre(request.getGenre());
        movie.setFormats(request.getFormats());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setStatus(request.getStatus());
    }

    private void handleShowtimeUpdate(Movie movie, Long showtimeId) throws BadRequestException {
        Showtime showtimeToBeSet = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SHOWTIME_NOT_FOUND));

        showtimeService.showtimeUpdateCheck(
                showtimeToBeSet.getId(),
                showtimeToBeSet.getHall().getId(),
                showtimeToBeSet.getDate(),
                showtimeToBeSet.getStartTime(),
                showtimeToBeSet.getEndTime()
        );

        movie.getShowtimes().add(showtimeToBeSet);
    }
}
