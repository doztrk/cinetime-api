package com.Cinetime.payload.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class MovieResponse {
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
    }

