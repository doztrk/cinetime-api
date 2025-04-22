package com.Cinetime.service;

import com.Cinetime.entity.*;
import com.Cinetime.enums.MovieStatus;
import com.Cinetime.helpers.MovieHelper;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.payload.dto.request.MovieRequest;
import com.Cinetime.payload.dto.response.MovieResponse;
import com.Cinetime.payload.mappers.MovieMapper;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.repo.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private final CinemaRepository cinemaRepository;


    public ResponseMessage<List<Movie>> getMovieByHall(int page, int size, String sort, String type, String hall) {
        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);

        List<Movie> movies = movieRepository.findByHalls_NameIgnoreCase(hall, pageable);

        return ResponseMessage.<List<Movie>>builder()
                .message("Movies found successfully")
                .httpStatus(HttpStatus.OK)
                .object(movies)
                .build();
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

    @Transactional
    public ResponseMessage<Movie> createMovie(MovieRequest movieRequest) {
        //Burasi frontendden de kontrol edilecek fakat biz yine de double layer security icin kontrolleri yapalim
        movieHelper.validateMovieRequest(movieRequest);

        Movie newMovie = movieMapper.mapMovieRequestToMovie(movieRequest);

        // Handle Hall relationship
        Optional<Hall> hallToBeSetOptional = hallRepository.findById(movieRequest.getHallId());

        if (hallToBeSetOptional.isEmpty()) {
            return ResponseMessage.<Movie>builder()
                    .message(ErrorMessages.HALL_NOT_FOUND)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }

        Hall hallToBeSet = hallToBeSetOptional.get();


        newMovie.getHalls().add(hallToBeSet);

        hallToBeSet.getMovies().add(newMovie);



    /*    Optional<Showtime> showtimeToBeSetOptional = showtimeRepository.findById(movieRequest.getShowtimeId());

        if (showtimeToBeSetOptional.isEmpty()) {
            return ResponseMessage.<Movie>builder()
                    .message(ErrorMessages.SHOWTIME_NOT_FOUND)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }
        Showtime showtimeToBeSet = showtimeToBeSetOptional.get();


        newMovie.getShowtimes().add(showtimeToBeSet);
        showtimeToBeSet.setMovie(newMovie);*/


        if (movieRequest.getPosterImage() != null && !movieRequest.getPosterImage().isEmpty()) {
            try {
                PosterImage posterImage = new PosterImage();
                posterImage.setName(movieRequest.getPosterImage().getOriginalFilename());
                posterImage.setType(movieRequest.getPosterImage().getContentType());
                posterImage.setData(movieRequest.getPosterImage().getBytes());

                newMovie.setPoster(posterImage);
            } catch (IOException e) {
                return ResponseMessage.<Movie>builder()
                        .message("Failed to process image: " + e.getMessage())
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .build();
            }
        }

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
    public ResponseMessage<List<MovieResponse>> getMoviesByCinemaSlug(String cinemaSlug) {


        List<MovieResponse> movieList = movieRepository.findMoviesByCinemaSlug(cinemaSlug);

        if (movieList.isEmpty()) {
            return ResponseMessage.<List<MovieResponse>>builder()
                    .message(ErrorMessages.MOVIE_NOT_FOUND)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

/*
        List<MovieResponse> movieListResponse = movieMapper.mapMovieToMovieResponse(movieList);
*/


        return ResponseMessage.<List<MovieResponse>>builder()
                .message("Movies found successfully")
                .httpStatus(HttpStatus.OK)
                .object(movieList)
                .build();
    }
}

