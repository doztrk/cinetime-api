package com.Cinetime.repo;

import com.Cinetime.entity.Movie;
import com.Cinetime.enums.MovieStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByStatus(MovieStatus status, Pageable pageable);

    List<Movie> findByHalls_NameIgnoreCase(String hallName, Pageable pageable);

    boolean existsBySlug(String slug);

    @Query("SELECT m FROM Movie m JOIN m.halls h JOIN h.cinema c WHERE c.slug = :slug")
    List<Movie> findByCinemaSlug(@Param("slug") String slug);

    @Query("SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Movie> findByTitleContainingIgnoreCase(@Param("query") String query, Pageable pageable);

    Page<Movie> findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(String titleQuery, String summaryQuery, Pageable pageable);
}



