package com.Cinetime.payload.dto.request.user;

import com.Cinetime.entity.PosterImage;
import com.Cinetime.enums.MovieStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class MovieRequest {

    @NotBlank(message = "Please provide a title")
    private String title;

    @NotBlank(message = "Please provide a slug")
    private String slug;

    @NotBlank(message = "Please provide a summary")
    private String summary;

    @NotNull(message = "Please provide a release date")
    private LocalDate releaseDate;

    @NotNull(message = "Please provide a duration")
    private Integer duration;

    @NotNull(message = "Please provide a rating")
    private Double rating;

    @NotNull(message = "Please provide a hall id")
    private Long hallId;

    @NotNull(message = "Please provide a showtime id")
    private Long showtimeId;

    @NotNull(message = "Please provide a poster image")
    private MultipartFile posterImage;

    @NotBlank(message = "Please provide a director")
    private String director;

    @NotEmpty(message = "Please provide casts")
    private List<String> cast;

    @NotEmpty(message = "Please provide formats")
    private List<String> formats;

    @NotEmpty(message = "Please provide genre")
    private List<String> genre;

    @NotBlank(message = "Please provide casts")
    private MovieStatus status;
}
