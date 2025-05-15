package com.Cinetime.helpers;

import com.Cinetime.helpers.abstracts.AbstractMovieValidatorHelper;
import com.Cinetime.payload.dto.request.MovieRequestUpdate;
import com.Cinetime.repo.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MovieHelperUpdate extends AbstractMovieValidatorHelper<MovieRequestUpdate> {

    public void validateMovieRequest(MovieRequestUpdate request) {
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
                request.getShowtimeId()
        );
    }
}
