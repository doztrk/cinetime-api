package com.Cinetime.service;

import com.Cinetime.entity.Showtime;
import com.Cinetime.helpers.MovieHelperUpdate;
import com.Cinetime.payload.dto.request.MovieRequestUpdate;
import com.Cinetime.payload.dto.response.CinemaResponse;
import com.Cinetime.payload.dto.response.MovieResponse;
import com.Cinetime.payload.dto.response.MovieResponseCinema;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.Cinetime.entity.Hall;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;


@Service
@RequiredArgsConstructor

public class MovieService {

    private final MovieRepository movieRepository;
    private final PageableHelper pageableHelper;
    private final MovieMapper movieMapper;
    private final HallRepository hallRepository;
    private final ShowtimeRepository showtimeRepository;
    private final MovieHelper movieHelper;
    private final ShowtimeService showtimeService;
    private final MovieHelperUpdate movieHelperUpdate;
    @Autowired
    private Environment env;

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

    public ResponseMessage<MovieResponse> createMovie(MovieRequest movieRequest) {
        // Validate input
        movieHelper.validateMovieRequest(movieRequest);

        Movie newMovie = movieMapper.mapMovieRequestToMovie(movieRequest);

        // Handle Hall relationship
        Hall hallToBeSet = hallRepository.findById(movieRequest.getHallId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.HALL_NOT_FOUND));
        newMovie.getHalls().add(hallToBeSet);
        hallToBeSet.getMovies().add(newMovie); // Update both sides of the relationship
/*
        // Handle Showtime relationship
        Showtime showtimeToBeSet = showtimeRepository.findById(movieRequest.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SHOWTIME_NOT_FOUND));
        newMovie.getShowtimes().add(showtimeToBeSet);
        showtimeToBeSet.setMovie(newMovie); // Update both sides of the relationship
        */


        if (movieRequest.getPosterImage() != null && !movieRequest.getPosterImage().isEmpty()) {
            try {
                String uploadDir = env.getProperty("file.upload-dir");
                String fileName = UUID.randomUUID() + "_" + movieRequest.getPosterImage().getOriginalFilename();
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(fileName);
                Files.copy(movieRequest.getPosterImage().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // URL'yi ayarla (örneğin: http://localhost:8080/uploads/abc.jpg)
                String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/uploads/image/")
                        .path(fileName)
                        .toUriString();

                newMovie.setPosterUrl(imageUrl);

            } catch (IOException e) {
                return ResponseMessage.<MovieResponse>builder()
                        .message("Failed to upload image: " + e.getMessage())
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .build();
            }
        }

        // Save the movie
        newMovie.setCreatedAt(LocalDateTime.now());
        newMovie.setUpdatedAt(LocalDateTime.now());
        Movie savedMovie = movieRepository.save(newMovie);

        return ResponseMessage.<MovieResponse>builder()
                .message(SuccessMessages.MOVIE_CREATE)
                .httpStatus(HttpStatus.CREATED)
                .object(movieMapper.mapMovieToMovieResponse(savedMovie))
                .build();
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

    public ResponseMessage<MovieResponse> updateMovie(Long movieId, MovieRequestUpdate movieRequest) throws BadRequestException {

        movieHelperUpdate.validateMovieRequest(movieRequest);

        Movie existingMovie = isMovieExist(movieId);

        existingMovie.setTitle(movieRequest.getTitle());
        existingMovie.setSlug(movieRequest.getSlug());
        existingMovie.setSummary(movieRequest.getSummary());
        existingMovie.setDuration(movieRequest.getDuration());
        existingMovie.setRating(movieRequest.getRating());
        existingMovie.setDirector(movieRequest.getDirector());
        existingMovie.setCast(movieRequest.getCast());
        existingMovie.setGenre(movieRequest.getGenre());
        existingMovie.setFormats(movieRequest.getFormats());
        existingMovie.setReleaseDate(movieRequest.getReleaseDate());
        existingMovie.setStatus(movieRequest.getStatus());

        // Handle Hall relationship update if provided
        if (movieRequest.getHallId() != null) {
            Hall hallToBeSet = hallRepository.findById(movieRequest.getHallId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.HALL_NOT_FOUND));
            //existingMovie.getHalls().clear();
            existingMovie.getHalls().add(hallToBeSet);
            hallToBeSet.getMovies().add(existingMovie);
        }


        // Handle Showtime relationship if necessary (optional)
        if (movieRequest.getShowtimeId() != null) {
            Showtime showtimeToBeSet = showtimeRepository.findById(movieRequest.getShowtimeId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SHOWTIME_NOT_FOUND));
            showtimeService.showtimeUpdateCheck(
                    showtimeToBeSet.getId(),
                    showtimeToBeSet.getHall().getId(),
                    showtimeToBeSet.getDate(),
                    showtimeToBeSet.getStartTime(),
                    showtimeToBeSet.getEndTime()
            );


            //existingMovie.getShowtimes().clear();  // Clear previous showtimes and set the new one
            existingMovie.getShowtimes().add(showtimeToBeSet);
            //showtimeToBeSet.getMovie(). // Update both sides of the relationship
        }

        // Handle poster image update (optional)
        if (movieRequest.getPosterImage() != null && !movieRequest.getPosterImage().isEmpty()) {
            try {
                String uploadDir = env.getProperty("file.upload-dir");
                String fileName = UUID.randomUUID().toString() + "_" + movieRequest.getPosterImage().getOriginalFilename();
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(fileName);
                Files.copy(movieRequest.getPosterImage().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Set the URL for the image
                String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/uploads/image/")
                        .path(fileName)
                        .toUriString();

                existingMovie.setPosterUrl(imageUrl);

            } catch (IOException e) {
                return ResponseMessage.<MovieResponse>builder()
                        .message("Failed to upload image: " + e.getMessage())
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
    }

    public ResponseMessage<?> deleteMovieById(Long movieId) {
        isMovieExist(movieId);
        return ResponseMessage.builder()
                .message(SuccessMessages.MOVIE_DELETE)
                .httpStatus(HttpStatus.OK)
                .build();
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


}
