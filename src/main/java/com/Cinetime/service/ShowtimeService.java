package com.Cinetime.service;

import com.Cinetime.entity.Showtime;
import com.Cinetime.payload.messages.NoShowTimesAvailableException;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.repo.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
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

    public ResponseEntity<List<Showtime>> getUpcomingShowtimes(Long movieId) {
        List<Showtime> allShowtimes = showtimeRepository.findByMovieId(movieId);// Tum showtimeler gelsin

        LocalDateTime now = LocalDateTime.now(); // Su anin tarih/saati

        if (allShowtimes.isEmpty()) { //Eger bossa noContent dondur
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(
                    allShowtimes.stream().filter(showtime -> {
                                LocalDateTime showTimeDateTime = LocalDateTime.of(showtime.getDate(), showtime.getStartTime());//Showtime'in sadece tarih ve saati gelsin
                                return showTimeDateTime.isAfter(now); //Showtime'i su andan sonra olanlari return et
                            })
                            .collect(Collectors.toList()));//Liste haline getir
        }
    }

}
