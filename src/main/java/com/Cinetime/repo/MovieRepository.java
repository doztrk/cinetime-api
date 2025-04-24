package com.Cinetime.repo;

import com.Cinetime.entity.Movie;
import com.Cinetime.entity.Showtime;
import com.Cinetime.enums.MovieStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    Page<Movie> findByStatus(MovieStatus status, Pageable pageable);

    Page<Movie> findByHalls_Id(Long hallId, Pageable pageable);

    boolean existsBySlug(String slug);

    // MovieId'ye ve startTime'ı şu andan sonra olan Showtime'lara göre sayfalı veri çekme
    @Query("SELECT s FROM Showtime s WHERE s.movie.id = :movieId AND s.startTime > :now ORDER BY " +
            "CASE WHEN :sort = 'asc' THEN s.startTime END ASC, " +
            "CASE WHEN :sort = 'desc' THEN s.startTime END DESC")
    Page<Showtime> findByMovieIdAndStartTimeAfter(@Param("movieId") Long movieId,
                                                  @Param("now") LocalDateTime now,
                                                  Pageable pageable,
                                                  @Param("sort") String sort);

    Page<Movie> findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(String titleQuery, String summaryQuery, Pageable pageable);
}



