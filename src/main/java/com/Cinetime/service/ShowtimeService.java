package com.Cinetime.service;

import com.Cinetime.entity.Movie;
import com.Cinetime.entity.Showtime;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.repo.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final PageableHelper pageableHelper;

    public ResponseMessage<Page<Showtime>> getUpcomingShowtimes(int page, int size, String sort, String type, Long movieId) {

        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);
        // Şu anki tarih saatinden sonrasında olan tüm showtime'ları alıyoruz
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Page<Showtime> showtimes = showtimeRepository.findUpcomingShowtimesByMovieId(movieId, today, now, pageable);

        if (showtimes.isEmpty()) {
            return ResponseMessage.<Page<Showtime>>builder()
                    .httpStatus(HttpStatus.OK)
                    .message("Showtimes not found for the given movie")
                    .build();
        }

        return ResponseMessage.<Page<Showtime>>builder()
                .httpStatus(HttpStatus.OK)
                .object(showtimes)
                .message("Showtimes found successfully")
                .build();
    }
}
