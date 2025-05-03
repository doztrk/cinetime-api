package com.Cinetime.service;

import com.Cinetime.entity.Showtime;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.dto.response.ShowtimeResponse;
import com.Cinetime.payload.mappers.ShowtimeMapper;
import com.Cinetime.repo.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
    private final ShowtimeMapper showtimeMapper;

    public ResponseMessage<Page<ShowtimeResponse>> getUpcomingShowtimes(int page, int size, String sort, String type, Long movieId) {

        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);
        // Şu anki tarih saatinden sonrasında olan tüm showtime'ları alıyoruz
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();


        Page<Showtime> showtimes = showtimeRepository.findUpcomingShowtimesByMovieId(movieId, today, now, pageable);

        if (showtimes.isEmpty()) {
            return ResponseMessage.<Page<ShowtimeResponse>>builder()

                    .httpStatus(HttpStatus.OK)
                    .message("Showtimes not found for the given movie")
                    .build();
        }

        return ResponseMessage.<Page<ShowtimeResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .object(showtimeMapper.mapShowtimePageToShowtimeResponse(showtimes))
                .message("Showtimes found successfully")
                .build();
    }

    public void showtimeCheck(Long hallId,LocalDate date,LocalTime startTime,LocalTime endTime) throws BadRequestException {

        boolean hasConflict = showtimeRepository.existsByHallIdAndDateAndTimeOverlap(
                hallId,
                date,
                startTime,
                endTime
        );
        if (hasConflict) {
            throw new BadRequestException("Bu saat aralığında bu salonda başka bir gösterim mevcut.");
        }
    }

    public void showtimeUpdateCheck(Long showtimeId, Long hallId,LocalDate date,LocalTime startTime,LocalTime endTime) throws BadRequestException {
        boolean hasConflict = showtimeRepository.existsConflictForUpdate(
                showtimeId,
                hallId,
                date,
                startTime,
                endTime
        );

        if (hasConflict) {
            throw new BadRequestException("Bu saat aralığında bu salonda başka bir gösterim mevcut.");
        }
    }
}
