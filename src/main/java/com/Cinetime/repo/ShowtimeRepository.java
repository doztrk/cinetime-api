package com.Cinetime.repo;

import com.Cinetime.entity.Showtime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {


    Page<Showtime> findByMovieIdAndStartTimeAfter(Long movieId, LocalDateTime now, Pageable pageable);
}
