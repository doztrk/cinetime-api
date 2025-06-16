package com.Cinetime.payload.dto.response;

import com.Cinetime.enums.MovieStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MovieResponse {
    private Long id;
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
    private MovieStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String posterUrl;


    // Parametreli constructor'ı doğru şekilde düzenliyoruz
    public MovieResponse(Long id, String title, String slug, String summary, LocalDate releaseDate,
                         Integer duration, Double rating, String director,
                         List<String> cast, List<String> formats, List<String> genre, MovieStatus status,
                         LocalDateTime createdAt, LocalDateTime updatedAt, String posterUrl
    ) {
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
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.posterUrl = posterUrl;

    }
}
