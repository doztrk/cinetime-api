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
    AND (s.date > :today OR 
         (s.date = :today AND s.startTime > :now))
    ORDER BY s.date, s.startTime
""")
    Page<Showtime> findUpcomingShowtimesByMovieId(
            @Param("movieId") Long movieId,
            @Param("today") LocalDate today,
            @Param("now") LocalTime now,
            Pageable pageable
    );

    @Query("""
    SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
    FROM Showtime s
    WHERE s.hall.id = :hallId
      AND s.date = :date
      AND (
            (:startTime < s.endTime AND :endTime > s.startTime)
          )
""")
    boolean existsByHallIdAndDateAndTimeOverlap(
            @Param("hallId") Long hallId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("""
    SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
    FROM Showtime s
    WHERE s.hall.id = :hallId
      AND s.date = :date
      AND (
          (:startTime < s.endTime AND :endTime > s.startTime)
      )
      AND s.id <> :showtimeId
""")
    boolean existsConflictForUpdate(
            @Param("showtimeId") Long showtimeId,
            @Param("hallId") Long hallId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
}
