package com.Cinetime.payload.dto.response;

import com.Cinetime.enums.MovieStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MovieResponse {

    private Long id;

    public MovieResponse(Long id, String title, String slug, String summary,
                         LocalDate releaseDate, Integer duration, Double rating,
                         String director, List<String> cast, List<String> formats,
                         List<String> genre, MovieStatus status) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.summary = summary;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.rating = rating;
        this.director = director;
        this.cast = cast;
        this.formats = formats;
        this.genre = genre;
        this.status = status.name(); // Convert enum to string
    }

    private String title;
    private String slug;
    private String summary;
    private LocalDate releaseDate;
    private Integer duration;
    private Double rating;
    private String director;
    private List<String> cast;
    private List<String> formats;
    private List<String> genre;
    private String status;
}

