package com.Cinetime.repo;

import com.Cinetime.entity.Movie;
import com.Cinetime.entity.Showtime;
import com.Cinetime.enums.MovieStatus;
import com.Cinetime.payload.dto.response.MovieResponse;
import com.Cinetime.payload.dto.response.MovieResponseCinema;
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


    boolean existsBySlug(String slug);

    // MovieId'ye ve startTime'ı şu andan sonra olan Showtime'lara göre sayfalı veri çekme
    @Query("SELECT s FROM Showtime s WHERE s.movie.id = :movieId AND s.startTime > :now ORDER BY " +
            "CASE WHEN :sort = 'asc' THEN s.startTime END ASC, " +
            "CASE WHEN :sort = 'desc' THEN s.startTime END DESC")
    Page<Showtime> findByMovieIdAndStartTimeAfter(@Param("movieId") Long movieId,
                                                  @Param("now") LocalDateTime now,
                                                  Pageable pageable,
                                                  @Param("sort") String sort);

    @Query("SELECT new com.Cinetime.payload.dto.response.MovieResponseCinema(m.id, m.title, m.slug, m.summary, " +
            "m.releaseDate, m.duration, m.rating, m.director, m.cast, m.formats, m.genre, m.status) " +
            "FROM Movie m JOIN m.showtimes s JOIN s.hall h JOIN h.cinema c WHERE c.slug = :cinemaSlug")
    List<MovieResponseCinema> findMoviesByCinemaSlug(@Param("cinemaSlug") String cinemaSlug);

    Page<Movie> findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(String titleQuery, String summaryQuery, Pageable pageable);


    @Query("SELECT m FROM Movie m WHERE m.title = :title")
    Optional<Movie> findByTitle(@Param("title") String title);

    @Query("SELECT DISTINCT m FROM Movie m JOIN m.showtimes s JOIN s.hall h WHERE LOWER(h.name) LIKE LOWER(CONCAT('%', :hallName, '%'))")
    Page<Movie> findMoviesByHallName(@Param("hallName") String hallName, Pageable pageable);
    //Page<Movie> findByHalls_NameIgnoreCase(String hallName, Pageable pageable);

    @Query("SELECT DISTINCT m FROM Movie m JOIN m.showtimes s WHERE s.hall.id = :hallId")
    Page<Movie> findMoviesByHallId(@Param("hallId") Long hallId, Pageable pageable);

}



