package com.Cinetime.repo;

import com.Cinetime.entity.Showtime;
import com.Cinetime.payload.dto.response.ShowtimeResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {


    @Query("SELECT new com.Cinetime.payload.dto.response.ShowtimeResponse(s.id, s.date, s.startTime, s.endTime, m.id, m.title, h.id, h.name) " +
            "FROM Showtime s JOIN s.movie m JOIN s.hall h " +
            "WHERE m.id = :movieId")
    List<ShowtimeResponse> findShowtimeDtosByMovieId(@Param("movieId") Long movieId);

}
