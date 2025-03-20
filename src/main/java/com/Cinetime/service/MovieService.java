package com.Cinetime.service;

import com.Cinetime.entity.Movie;
import com.Cinetime.enums.MovieStatus;
import com.Cinetime.repo.MovieRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<Movie> getComingSoonMovies(Pageable pageable) {
        return movieRepository.findByStatus(MovieStatus.COMING_SOON, pageable);
    }

    public Movie createMovie(Movie movie) {
        if (movieRepository.existsBySlug(movie.getSlug())) {
            throw new IllegalArgumentException("Movie with this slug already exists.");
        }

        movie.setCreatedAt(LocalDate.now());
        movie.setUpdatedAt(LocalDate.now());

        return movieRepository.save(movie);
    }
}

