package com.Cinetime.service;

import com.Cinetime.entity.Showtime;
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

    private ShowtimeRepository showtimeRepository;

    public List<Showtime> getUpcomingShowtimes(Long movieId){
        List<Showtime> allShowtimes = showtimeRepository.findByMovieId(movieId);
        return allShowtimes.stream()
                .filter(showtime -> showtime.getStartTime().isAfter(LocalTime.from(LocalDateTime.now())))
                .collect(Collectors.toList());
    }
}
