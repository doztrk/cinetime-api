package com.Cinetime.repo;

import com.Cinetime.entity.Showtime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {


    @Query("""
    SELECT s FROM Showtime s 
    WHERE s.movie.id = :movieId 
    AND (s.date > :today OR (s.date = :today AND s.startTime > :now))
""")
    Page<Showtime> findUpcomingShowtimesByMovieId(
            @Param("movieId") Long movieId,
            @Param("today") LocalDate today,
            @Param("now") LocalTime now,
            Pageable pageable
    );
}
