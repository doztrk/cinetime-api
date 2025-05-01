package com.Cinetime.service;

import com.Cinetime.entity.Showtime;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.dto.response.ShowtimeResponse;
import com.Cinetime.payload.mappers.ShowtimeMapper;
import com.Cinetime.repo.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final ShowtimeMapper showtimeMapper;

    public ResponseMessage<List<ShowtimeResponse>> getUpcomingShowtimes(Long movieId) {
        List<ShowtimeResponse> allShowtimes = showtimeRepository.findShowtimeDtosByMovieId(movieId);


        if (allShowtimes.isEmpty()) {
            return ResponseMessage.<List<ShowtimeResponse>>builder()
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .message("Showtimes not found for the given movie")
                    .build();
        } else {

            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();

            List<ShowtimeResponse> showtimes = allShowtimes.stream()
                    .filter(showtime ->
                            showtime.getDate().isAfter(today) ||
                                    (showtime.getDate().isEqual(today) && showtime.getStartTime().isAfter(now))
                    )
                    .toList();
            return ResponseMessage.<List<ShowtimeResponse>>builder()
                    .httpStatus(HttpStatus.OK)
                    .object(showtimes)
                    .message("Showtimes found successfully")
                    .build();

        }
    }

}
