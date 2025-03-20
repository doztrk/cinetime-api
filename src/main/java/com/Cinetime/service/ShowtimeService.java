package com.Cinetime.service;

import com.Cinetime.entity.Showtime;
import com.Cinetime.payload.messages.NoShowTimesAvailableException;
import com.Cinetime.repo.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;

    public List<Showtime> getUpcomingShowtimes(Long movieId) {
        List<Showtime> allShowtimes = showtimeRepository.findByMovieId(movieId);

        List<Showtime> upcomingShowtimes = allShowtimes.stream()
                .filter(showtime -> showtime.getStartTime()
                                .isAfter(LocalTime.from(LocalDateTime.now())))
                                .collect(Collectors.toList());

        if (upcomingShowtimes.isEmpty()) {
            throw new NoShowTimesAvailableException("No upcoming showtimes found for movie ID: " + movieId);
        }

        return upcomingShowtimes;
    }

}
