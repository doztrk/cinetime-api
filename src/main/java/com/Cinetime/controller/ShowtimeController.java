package com.Cinetime.controller;

import com.Cinetime.entity.Showtime;
import com.Cinetime.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movies")
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    @GetMapping("/{movieId}/show-times")
    public ResponseEntity<List<Showtime>> getUpcomingShowtimes(@PathVariable Long movieId) {
        //Business service'e aktarildi.
        return showtimeService.getUpcomingShowtimes(movieId);
    }
}
