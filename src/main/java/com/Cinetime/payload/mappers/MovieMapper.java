package com.Cinetime.payload.mappers;

import com.Cinetime.entity.Hall;
import com.Cinetime.entity.Movie;
import com.Cinetime.entity.Showtime;
import com.Cinetime.enums.MovieStatus;
import com.Cinetime.payload.dto.request.MovieRequest;
import com.Cinetime.payload.dto.response.MovieResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MovieMapper {


    public Movie mapMovieRequestToMovie(MovieRequest movieRequest) {

        return Movie.builder()
                .title(movieRequest.getTitle())
                .slug(movieRequest.getSlug())
                .summary(movieRequest.getSummary())
                .releaseDate(movieRequest.getReleaseDate())
                .duration(movieRequest.getDuration())
                .rating(movieRequest.getRating())
                //Hall service'te setlenecek
                //posterImageId service'te setlenecek
                //
                .director(movieRequest.getDirector())
                .cast(movieRequest.getCast())
                .formats(movieRequest.getFormats())
                .genre(movieRequest.getGenre())
                .status(movieRequest.getStatus() != null ?
                        movieRequest.getStatus() : MovieStatus.COMING_SOON)
                .build();
    }

    public MovieResponse mapMovieToMovieResponse(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .slug(movie.getSlug())
                .summary(movie.getSummary())
                .releaseDate(movie.getReleaseDate())
                .duration(movie.getDuration())
                .rating(movie.getRating())
                .director(movie.getDirector())
                .cast(movie.getCast())
                .formats(movie.getFormats())
                .genre(movie.getGenre())
                .status(movie.getStatus().name())
                .build();
    }

    public Page<MovieResponse> mapMoviePageToMovieResponse(Page<Movie> movies) {
        return movies.map(this::mapMovieToMovieResponse);
    }

}
