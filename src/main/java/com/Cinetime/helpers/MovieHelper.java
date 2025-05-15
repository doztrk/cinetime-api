package com.Cinetime.helpers;

import com.Cinetime.helpers.abstracts.AbstractMovieValidatorHelper;
import com.Cinetime.payload.dto.request.MovieRequest;
import com.Cinetime.payload.dto.request.MovieRequestUpdate;
import com.Cinetime.repo.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MovieHelper extends AbstractMovieValidatorHelper<MovieRequest> {

    public void validateMovieRequest(MovieRequest request) {
        validateCommonFields(
                request,
                request.getTitle(),
                request.getSlug(),
                request.getSummary(),
                request.getDirector(),
                request.getDuration(),
                request.getCast(),
                request.getFormats(),
                request.getGenre(),
                request.getPosterImage(),
                null // showtimeId bu request'te yok
        );
    }
}
