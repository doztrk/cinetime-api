package com.Cinetime.payload.mappers;

import com.Cinetime.entity.Showtime;
import com.Cinetime.payload.dto.response.HallResponse;
import com.Cinetime.payload.dto.response.MovieResponse;
import com.Cinetime.payload.dto.response.ShowtimeResponse;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@Data
public class ShowtimeMapper {


    public ShowtimeResponse mapShowtimeToShowtimeResponse(Showtime showtime) {
        return ShowtimeResponse.builder()
                .id(showtime.getId())
                .date(showtime.getDate())
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                .movie(MovieResponse.builder()
                        .id(showtime.getMovie().getId())
                        .title(showtime.getMovie().getTitle())
                        .slug(showtime.getMovie().getSlug())
                        .summary(showtime.getMovie().getSummary())
                        .releaseDate(showtime.getMovie().getReleaseDate())
                        .duration(showtime.getMovie().getDuration())
                        .rating(showtime.getMovie().getRating())
                        .director(showtime.getMovie().getDirector())
                        .cast(showtime.getMovie().getCast())
                        .formats(showtime.getMovie().getFormats())
                        .genre(showtime.getMovie().getGenre())
                        .status(showtime.getMovie().getStatus())
                        .createdAt(showtime.getMovie().getCreatedAt())
                        .updatedAt(showtime.getMovie().getUpdatedAt())
                        .posterUrl(showtime.getMovie().getPosterUrl())
                        .build())
                .hall(HallResponse.builder()
                        .id(showtime.getHall().getId())
                        .name(showtime.getHall().getName())
                        .seatCapacity(showtime.getHall().getSeatCapacity())
                        .isSpecial(showtime.getHall().getIsSpecial())
                        .build())
                .createdAt(showtime.getCreatedAt())
                .updatedAt(showtime.getUpdatedAt())
                .build();
    }

    public Page<ShowtimeResponse> mapShowtimePageToShowtimeResponse(Page<Showtime> showtimes) {
        return showtimes.map(this::mapShowtimeToShowtimeResponse);
    }
}
