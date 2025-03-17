package com.Cinetime.Service;

import com.Cinetime.Repository.MovieRepository;
import com.Cinetime.entity.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    public Movie updateMovie(Long id, Movie movieDetails) {
        Optional<Movie> movieOptional = movieRepository.findById(id);
        if (!movieOptional.isPresent()) {
            return null;
        }

        Movie movie = movieOptional.get();
        movie.setTitle(movieDetails.getTitle());
        movie.setSlug(movieDetails.getSlug());
        movie.setSummary(movieDetails.getSummary());
        movie.setReleaseDate(movieDetails.getReleaseDate());
        movie.setDuration(movieDetails.getDuration());
        movie.setRating(movieDetails.getRating());
        movie.setDirector(movieDetails.getDirector());
        movie.setCast(movieDetails.getCast());
        movie.setFormats(movieDetails.getFormats());
        movie.setGenre(movieDetails.getGenre());
        movie.setPoster(movieDetails.getPoster());
        movie.setStatus(movieDetails.getStatus());
        movie.setUpdatedAt(LocalDate.now());

        return movieRepository.save(movie);
    }

    public boolean deleteMovie(Long id) {
        Optional<Movie> movieOptional = movieRepository.findById(id);
        if (!movieOptional.isPresent()) {
            return false;
        }

        movieRepository.delete(movieOptional.get());
        return true;  // Movie deleted
    }
}



