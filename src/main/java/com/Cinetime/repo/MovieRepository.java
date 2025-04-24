package com.Cinetime.repo;

import com.Cinetime.entity.Movie;
import com.Cinetime.enums.MovieStatus;
import com.Cinetime.payload.dto.response.MovieResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    @Query("SELECT new com.Cinetime.payload.dto.response.MovieResponse(" +
            "m.id, m.title, m.slug, m.summary, m.releaseDate, m.duration, " +
            "m.rating, m.director, m.cast, m.formats, m.genre, m.status) " +
            "FROM Movie m " +
            "WHERE m.status = :status")
    List<MovieResponse> findByStatus(@Param("status") MovieStatus status, Pageable pageable);


    List<Movie> findByHalls_NameIgnoreCase(String hallName, Pageable pageable);

    boolean existsBySlug(String slug);

    @Query("SELECT new com.Cinetime.payload.dto.response.MovieResponse(m.id, m.title, m.slug, m.summary, " +
            "m.releaseDate, m.duration, m.rating, m.director, m.cast, m.formats, m.genre, m.status) " +
            "FROM Movie m JOIN m.halls h JOIN h.cinema c WHERE c.slug = :cinemaSlug")
    List<MovieResponse> findMoviesByCinemaSlug(@Param("cinemaSlug") String cinemaSlug);

    Page<Movie> findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(String titleQuery, String summaryQuery, Pageable pageable);

}



