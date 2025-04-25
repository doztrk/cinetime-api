package com.Cinetime.service;
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
import com.Cinetime.repo.PosterImageRepository;
import com.Cinetime.repo.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

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


    public ResponseMessage<Page<Movie>> getMovieByHall(int page, int size, String sort, String type, Long hallId) {
        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);

        Page<Movie> pageResult = movieRepository.findByHalls_Id(hallId, pageable);

        return ResponseMessage.<Page<Movie>>builder()
                .message("Movies found successfully")
                .httpStatus(HttpStatus.OK)
                .object(pageResult)
                .build();
    }

    public ResponseEntity<Page<Movie>> getInTheatersMovies(int page, int size, String sort, String type) {

        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);

        Page<Movie> movies = movieRepository.findByStatus(MovieStatus.IN_THEATERS, pageable);

        if (movies.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(movies);
        }
    }

    public ResponseEntity<Page<Movie>> getComingSoonMovies(int page, int size, String sort, String type) {
        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);

        Page<Movie> movies = movieRepository.findByStatus(MovieStatus.COMING_SOON, pageable);

        if (movies.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(movies);
        }
    }

    @Autowired
    private Environment env;
    public ResponseMessage<Movie> createMovie(MovieRequest movieRequest) {
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
        */


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

                // URL'yi ayarla (örneğin: http://localhost:8080/uploads/abc.jpg)
                String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/uploads/image/")
                        .path(fileName)
                        .toUriString();

                newMovie.setPosterUrl(imageUrl); // PosterImage yerine URL string

            } catch (IOException e) {
                return ResponseMessage.<Movie>builder()
                        .message("Failed to upload image: " + e.getMessage())
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

    //M01 - Get Movies By Query
    public ResponseMessage<Page<Movie>> getMoviesByQuery(String q, int page, int size, String sort, String type) {
        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);


        Page<Movie> movies = movieRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(q, q, pageable);


        return ResponseMessage.<Page<Movie>>builder()
                .message("Movies found successfully")
                .httpStatus(HttpStatus.OK)
                .object(movies)
                .build();
    }


    // M02 - Return Movies Based on Cinema Slug
    public ResponseMessage<Page<MovieResponse>> getMoviesByCinemaSlug(String cinemaSlug, int page, int size, String sort, String type) {
        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);

        Page<MovieResponse> moviePage = movieRepository.findMoviesByCinemaSlug(cinemaSlug, pageable);

        if (moviePage.isEmpty()) {
            return ResponseMessage.<Page<MovieResponse>>builder()
                    .message(ErrorMessages.MOVIE_NOT_FOUND)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        return ResponseMessage.<Page<MovieResponse>>builder()
                .message("Movies found successfully")
                .httpStatus(HttpStatus.OK)
                .object(moviePage)
                .build();
    }


}

