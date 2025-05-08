package com.Cinetime.payload.mappers;

import com.Cinetime.entity.Hall;
import com.Cinetime.entity.Movie;
import com.Cinetime.entity.Showtime;
import com.Cinetime.payload.dto.request.ShowtimeRequest;
import com.Cinetime.payload.dto.response.CinemaResponse;
import com.Cinetime.payload.dto.response.HallResponse;
import com.Cinetime.payload.dto.response.MovieResponse;
import com.Cinetime.payload.dto.response.ShowtimeResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@Data
@RequiredArgsConstructor
public class ShowtimeMapper {

    private final HallMapper hallMapper;


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
                        .halls(hallMapper.mapHallToHallResponse(showtime.getMovie().getHalls()))
                        .build())
                .hall(HallResponse.builder()
                        .id(showtime.getHall().getId())
                        .name(showtime.getHall().getName())
                        .seatCapacity(showtime.getHall().getSeatCapacity())
                        .isSpecial(showtime.getHall().getIsSpecial())
                        .build())
                .cinema(CinemaResponse.builder() // Add cinema information
                        .id(showtime.getHall().getCinema().getId())
                        .name(showtime.getHall().getCinema().getName())
                        .address(showtime.getHall().getCinema().getAddress())
                        .city(showtime.getHall().getCinema().getCity() != null ?
                                showtime.getHall().getCinema().getCity().getName() : null)
                        .district(showtime.getHall().getCinema().getDistrict() != null ?
                                showtime.getHall().getCinema().getDistrict().getName() : null)
                        .build())
                .price(showtime.getPrice())
                .createdAt(showtime.getCreatedAt())
                .updatedAt(showtime.getUpdatedAt())
                .build();
    }

    public Page<ShowtimeResponse> mapShowtimePageToShowtimeResponse(Page<Showtime> showtimes) {
        return showtimes.map(this::mapShowtimeToShowtimeResponse);
    }
    public Showtime mapShowtimeRequestToShowtime(ShowtimeRequest showtimeRequest, Movie movie, Hall hall,Double price){
        return Showtime.builder()
                .date(showtimeRequest.getDate())
                .startTime(showtimeRequest.getStartTime())
                .endTime(showtimeRequest.getEndTime())
                .movie(movie)
                .hall(hall)
                .price(price)
                .build();
    }

}
