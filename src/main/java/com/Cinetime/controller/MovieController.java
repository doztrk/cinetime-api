package com.Cinetime.controller;

import com.Cinetime.entity.Movie;
import com.Cinetime.payload.dto.MovieRequest;
import com.Cinetime.payload.response.MovieResponse;
import com.Cinetime.payload.response.ResponseMessage;
import com.Cinetime.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private MovieService movieService;

    //M12
    @PutMapping("/{movieId}")
    public ResponseEntity<MovieResponse> updateMovie(@PathVariable Long movieId, @RequestBody MovieRequest movieRequest) {
        MovieResponse updatedMovie = movieService.updateMovie(movieId, movieRequest);
        if (updatedMovie == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedMovie);
    }

    //M13
    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long movieId) {
        boolean deleted = movieService.deleteMovie(movieId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    //M03
    @GetMapping("/{hall}")
    public ResponseEntity<List<Movie>>getMovieByHall(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "releaseDate") String sort, @RequestParam(defaultValue = "asc") String type,
            @PathVariable String hall){

        return movieService.getMovieByHall(page,size,sort,type,hall);
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
}