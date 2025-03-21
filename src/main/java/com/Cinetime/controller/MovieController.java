package com.Cinetime.controller;

import com.Cinetime.entity.Movie;
import com.Cinetime.payload.dto.MovieRequest;
import com.Cinetime.payload.response.ResponseMessage;
import com.Cinetime.service.MovieService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;


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
    @PreAuthorize("permitAll()")  // Explicitly allow all access to this method
    public ResponseMessage<Movie> createMovie(HttpServletRequest request, @ModelAttribute MovieRequest movieRequest) {
        System.out.println("Request URI: " + request.getRequestURI());

        return movieService.createMovie(movieRequest);
    }
}

