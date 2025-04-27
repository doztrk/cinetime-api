package com.Cinetime.payload.mappers;

import com.Cinetime.entity.Showtime;
import com.Cinetime.payload.dto.response.ShowtimeResponse;
import org.springframework.stereotype.Component;

@Component
public class ShowtimeMapper {


    public ShowtimeResponse mapShowtimeToShowtimeResponse(Showtime showtime) {
        return ShowtimeResponse.builder()
                .id(showtime.getId())
                .date(showtime.getDate())
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                .movieId(showtime.getMovie().getId())
                .movieTitle(showtime.getMovie().getTitle())
                .hallId(showtime.getHall().getId())
                .hallName(showtime.getHall().getName())
                .build();
    }
}
