package com.Cinetime.helpers;

import com.Cinetime.payload.dto.request.user.MovieRequest;
import com.Cinetime.repo.MovieRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MovieHelper {

    private final MovieRepository movieRepository;

    public void validateMovieRequest(MovieRequest movieRequest) {
        List<String> validationErrors = new ArrayList<>();


        if (movieRequest.getTitle() == null || movieRequest.getTitle().trim().length() < 3 || movieRequest.getTitle().length() > 100) {
            validationErrors.add("Title must be between 3 and 100 characters");
        }

        if (movieRequest.getSlug() == null || movieRequest.getSlug().trim().length() < 5 || movieRequest.getSlug().length() > 20) {
            validationErrors.add("Slug must be between 3 and 50 characters");
        }

        if (movieRequest.getSummary() == null || movieRequest.getSummary().trim().length() < 3 || movieRequest.getSummary().length() > 300) {
            validationErrors.add("Summary must be between 3 and 300 characters");
        }

        if (movieRequest.getDirector() == null || movieRequest.getDirector().trim().isEmpty()) {
            validationErrors.add("Director cannot be empty");
        }


        if (movieRequest.getReleaseDate() == null) {
            validationErrors.add("Release date is required");
        }


        if (movieRequest.getDuration() == null || movieRequest.getDuration() <= 0) {
            validationErrors.add("Duration must be a positive integer (in minutes)");
        }


        if (movieRequest.getCast() == null || movieRequest.getCast().isEmpty()) {
            validationErrors.add("Cast list cannot be empty");
        }

        if (movieRequest.getFormats() == null || movieRequest.getFormats().isEmpty()) {
            validationErrors.add("Formats list cannot be empty");
        }

        if (movieRequest.getGenre() == null || movieRequest.getGenre().isEmpty()) {
            validationErrors.add("Genre list cannot be empty");
        }


        if (movieRequest.getHallId() == null) {
            validationErrors.add("Hall ID is required");
        }

//        if (movieRequest.getShowtimeId() == null) {
//            validationErrors.add("Showtime ID is required");
//        }


        if (movieRequest.getPosterImage() == null || movieRequest.getPosterImage().isEmpty()) {
            validationErrors.add("Poster image is required");
        } else {

            String contentType = movieRequest.getPosterImage().getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                validationErrors.add("Poster must be an image file");
            }


            if (movieRequest.getPosterImage().getSize() > 5 * 1024 * 1024) {
                validationErrors.add("Poster image size should not exceed 5MB");
            }
        }


        if (movieRepository.existsBySlug(movieRequest.getSlug())) {
            validationErrors.add("A movie with this slug already exists");
        }


        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Movie validation failed: " + String.join(", ", validationErrors));
        }
    }
}
