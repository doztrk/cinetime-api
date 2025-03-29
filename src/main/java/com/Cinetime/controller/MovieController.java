package com.Cinetime.controller;

import com.Cinetime.entity.Movie;
import com.Cinetime.entity.Showtime;
import com.Cinetime.payload.dto.MovieRequest;
import com.Cinetime.payload.response.ResponseMessage;
import com.Cinetime.service.MovieService;
import com.Cinetime.service.ShowtimeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movie")
public class MovieController {

    private final MovieService movieService;
    private final ShowtimeService showtimeService;

    //M03
    @GetMapping("/{hall}")
    public ResponseEntity<List<Movie>> getMovieByHall(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "releaseDate") String sort, @RequestParam(defaultValue = "asc") String type,
            @PathVariable String hall) {

        return movieService.getMovieByHall(page, size, sort, type, hall);
    }

    //M04
    @GetMapping("/in-theaters")
    public ResponseEntity<List<Movie>> getInTheatersMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "releaseDate") String sort,
            @RequestParam(defaultValue = "asc") String type) {

        return movieService.getInTheatersMovies(page, size, sort, type);
    }

    //M05
    @GetMapping("/coming-soon")
    public ResponseEntity<List<Movie>> getComingSoonMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "releaseDate") String sort,
            @RequestParam(defaultValue = "asc") String type) {

        return movieService.getComingSoonMovies(page, size, sort, type);
    }


    //M11
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Explicitly allow all access to this method
    public ResponseMessage<Movie> createMovie(@ModelAttribute MovieRequest movieRequest) {
        return movieService.createMovie(movieRequest);
    }


    //M14
    @GetMapping("/{movieId}/show-times")
    public ResponseEntity<List<Showtime>> getUpcomingShowtimes(@PathVariable Long movieId) {
        //Business service'e aktarildi.
        return showtimeService.getUpcomingShowtimes(movieId);
    }
}

