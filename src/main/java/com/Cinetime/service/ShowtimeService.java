package com.Cinetime.service;

import com.Cinetime.entity.Hall;
import com.Cinetime.entity.Movie;
import com.Cinetime.entity.Showtime;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.helpers.TicketPriceHelper;
import com.Cinetime.payload.dto.request.ShowtimeRequest;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.dto.response.ShowtimeResponse;
import com.Cinetime.payload.mappers.ShowtimeMapper;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.repo.HallRepository;
import com.Cinetime.repo.MovieRepository;
import com.Cinetime.repo.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final PageableHelper pageableHelper;
    private final ShowtimeMapper showtimeMapper;
    private final MovieRepository movieRepository;
    private final HallRepository hallRepository;
    private final TicketPriceHelper ticketPriceHelper;

    public ResponseMessage<Page<ShowtimeResponse>> getUpcomingShowtimesForMovieAndCinema(int page, int size, String sort, String type, Long movieId) {

        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);
        // Şu anki tarih saatinden sonrasında olan tüm showtime'ları alıyoruz
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();


        Page<Showtime> showtimes = showtimeRepository.findUpcomingShowtimesByMovieId(movieId, today, now, pageable);

        if (showtimes.isEmpty()) {
            return ResponseMessage.<Page<ShowtimeResponse>>builder()

                    .httpStatus(HttpStatus.NO_CONTENT)
                    .message("Showtimes not found for the given movie")
                    .build();
        }

        return ResponseMessage.<Page<ShowtimeResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .object(showtimeMapper.mapShowtimePageToShowtimeResponse(showtimes))
                .message("Showtimes found successfully")
                .build();
    }

    public void showtimeCheck(Long hallId, LocalDate date, LocalTime startTime, LocalTime endTime) throws BadRequestException {

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

    public void showtimeUpdateCheck(Long showtimeId, Long hallId, LocalDate date, LocalTime startTime, LocalTime endTime) throws BadRequestException {
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

    public ResponseMessage<ShowtimeResponse> getShowtimeById(Long showtimeId) {
        Optional<Showtime> showtimeOptional = showtimeRepository.findById(showtimeId);

        if (showtimeOptional.isEmpty()) {
            return ResponseMessage.<ShowtimeResponse>builder()
                    .message(ErrorMessages.SHOWTIME_NOT_FOUND)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        Showtime showtime = showtimeOptional.get();
        return ResponseMessage.<ShowtimeResponse>builder()
                .message(SuccessMessages.SHOWTIME_FOUND)
                .httpStatus(HttpStatus.OK)
                .object(showtimeMapper.mapShowtimeToShowtimeResponse(showtime))
                .build();

    }

    public ResponseMessage<ShowtimeResponse> createShowtimeForMovie(ShowtimeRequest showtimeRequest) {

        Optional<Movie> movieOptional = movieRepository.findById(showtimeRequest.getMovieId());
        if (movieOptional.isEmpty()) {
            return ResponseMessage.<ShowtimeResponse>builder()
                    .message(ErrorMessages.MOVIE_NOT_FOUND)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        Movie movie = movieOptional.get();

        Optional<Hall> hallOptional = hallRepository.findById(showtimeRequest.getHallId());
        if (hallOptional.isEmpty()) {
            return ResponseMessage.<ShowtimeResponse>builder()
                    .message(ErrorMessages.HALL_NOT_FOUND)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        Hall hall = hallOptional.get();
        LocalDate date = showtimeRequest.getDate();
        LocalTime startTime = showtimeRequest.getStartTime();
        LocalTime endTime = showtimeRequest.getEndTime();


        Double showtimePrice = ticketPriceHelper.calculateTicketPrice(hall, movie, startTime, endTime, date);

        Showtime showtime = showtimeMapper.mapShowtimeRequestToShowtime(showtimeRequest, movie, hall, showtimePrice);


        showtimeRepository.save(showtime);

        return ResponseMessage.<ShowtimeResponse>builder()
                .message(SuccessMessages.SHOWTIME_CREATED_SUCCESSFULLY)
                .httpStatus(HttpStatus.OK)
                .object(showtimeMapper.mapShowtimeToShowtimeResponse(showtime))
                .build();

    }

    public ResponseMessage<Page<ShowtimeResponse>> getUpcomingShowtimesForMovieAndCinema(
            int page, int size, String sort, String type, Long movieId, Long cinemaId) {

        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Page<Showtime> showtimes;

        if (cinemaId != null) {
            // Filter showtimes by movie, cinema, and current/future time
            showtimes = showtimeRepository.findUpcomingShowtimesByMovieAndCinema(
                    movieId, cinemaId, today, now, pageable
            );
        } else {
            // Fall back to existing method if no cinema is specified
            showtimes = showtimeRepository.findUpcomingShowtimesByMovieId(
                    movieId, today, now, pageable
            );
        }

        if (showtimes.isEmpty()) {
            return ResponseMessage.<Page<ShowtimeResponse>>builder()
                    .httpStatus(HttpStatus.OK)
                    .message("No showtimes found for the given criteria")
                    .build();
        }

        return ResponseMessage.<Page<ShowtimeResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .object(showtimeMapper.mapShowtimePageToShowtimeResponse(showtimes))
                .message("Showtimes found successfully")
                .build();
    }
}
