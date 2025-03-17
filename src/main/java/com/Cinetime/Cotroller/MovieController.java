package com.Cinetime.Cotroller;

import com.Cinetime.Service.MovieService;
import com.Cinetime.entity.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movies")

public class MovieController {

    @Autowired
    private MovieService movieService;

    @PutMapping("/id")
    public ResponseEntity<Movie> updateMovie(@PathVariable Long id, @RequestBody Movie movieDetails) {
        Movie updatedMovie = movieService.updateMovie(id, movieDetails);

        if (updatedMovie == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedMovie);
    }

    @DeleteMapping("/id")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        boolean deleted = movieService.deleteMovie(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
