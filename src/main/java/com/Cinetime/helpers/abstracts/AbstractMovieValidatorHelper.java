package com.Cinetime.helpers.abstracts;

import com.Cinetime.repo.MovieRepository;
import jakarta.validation.ValidationException;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Component
@NoArgsConstructor
public abstract class AbstractMovieValidatorHelper<T> {

    @Autowired
    protected MovieRepository movieRepository;

    // Parametreli constructor.
    public AbstractMovieValidatorHelper(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    protected void validateCommonFields(T movieRequest,
                                        String title,
                                        String slug,
                                        String summary,
                                        String director,
                                        Integer duration,
                                        List<String> cast,
                                        List<String> formats,
                                        List<String> genre,
                                        Long hallId,
                                        MultipartFile posterImage,
                                        Long showtimeId) {

        List<String> validationErrors = new ArrayList<>();

        // Validation logic

        if (title == null || title.trim().length() < 3 || title.length() > 100) {
            validationErrors.add("Title must be between 3 and 100 characters");
        }

        if (slug == null || slug.trim().length() < 5 || slug.length() > 20) {
            validationErrors.add("Slug must be between 3 and 50 characters");
        }

        if (summary == null || summary.trim().length() < 3 || summary.length() > 300) {
            validationErrors.add("Summary must be between 3 and 300 characters");
        }

        if (director == null || director.trim().isEmpty()) {
            validationErrors.add("Director cannot be empty");
        }

        if (duration == null || duration <= 0) {
            validationErrors.add("Duration must be a positive integer (in minutes)");
        }

        if (cast == null || cast.isEmpty()) {
            validationErrors.add("Cast list cannot be empty");
        }

        if (formats == null || formats.isEmpty()) {
            validationErrors.add("Formats list cannot be empty");
        }

        if (genre == null || genre.isEmpty()) {
            validationErrors.add("Genre list cannot be empty");
        }

        if (hallId == null) {
            validationErrors.add("Hall ID is required");
        }

        if (showtimeId != null && showtimeId < 0) {
            validationErrors.add("Invalid showtime ID");
        }

        if (posterImage == null || posterImage.isEmpty()) {
            validationErrors.add("Poster image is required");
        } else {
            String contentType = posterImage.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                validationErrors.add("Poster must be an image file");
            }

            if (posterImage.getSize() > 5 * 1024 * 1024) {
                validationErrors.add("Poster image size should not exceed 5MB");
            }
        }

        if (slug != null && movieRepository.existsBySlug(slug)) {
            validationErrors.add("A movie with this slug already exists");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Movie validation failed: " + String.join(", ", validationErrors));
        }
    }
}
