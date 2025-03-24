package com.Cinetime.controller;

import com.Cinetime.entity.Movie;
import com.Cinetime.payload.dto.MovieRequest;
import com.Cinetime.payload.response.MovieResponse;
import com.Cinetime.payload.response.ResponseMessage;
import com.Cinetime.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

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

    //M01
    @GetMapping
    public ResponseEntity<Page<Movie>> getMoviesByQuery(
            @RequestParam(required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "title") String sort,
            @RequestParam(value = "type", defaultValue = "asc") String type
    ) {
        return ResponseEntity.ok(movieService.getMoviesByQuery(q, page, size, sort, type));
    }

    // M02 - Return Movies Based on Cinema Slug
    @GetMapping("/{slug}")
    public ResponseEntity<List<Movie>> getMoviesByCinemaSlug(@PathVariable String slug) {
        List<Movie> movies = movieService.getMoviesByCinemaSlug(slug);
        return ResponseEntity.ok(movies);
    }




}

