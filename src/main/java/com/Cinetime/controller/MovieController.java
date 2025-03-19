package com.Cinetime.controller;

import com.Cinetime.entity.Movie;
import com.Cinetime.service.MovieService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    // GET /api/movies/coming-soon
    @GetMapping("/coming-soon")
    public ResponseEntity<List<Movie>> getComingSoonMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "releaseDate") String sort,
            @RequestParam(defaultValue = "asc") String type) {

        Pageable pageable = PageRequest.of(page, size,
                type.equalsIgnoreCase("desc") ? Sort.by(sort).descending() : Sort.by(sort).ascending());

        return ResponseEntity.ok(movieService.getComingSoonMovies(pageable));
    }

    // POST /api/movies (Admin Only)
    @PostMapping
    public ResponseEntity<Movie> createMovie(@RequestBody Movie movie) {
        try {
            Movie createdMovie = movieService.createMovie(movie);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMovie);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}

