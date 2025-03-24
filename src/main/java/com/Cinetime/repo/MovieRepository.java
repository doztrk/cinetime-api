package com.Cinetime.repo;

import com.Cinetime.entity.Movie;
import com.Cinetime.enums.MovieStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByStatus(MovieStatus status, Pageable pageable);

    List<Movie> findByHalls_NameIgnoreCase(String hallName, Pageable pageable);

    boolean existsBySlug(String slug);

    @Query("SELECT m FROM Movie m JOIN m.halls h JOIN h.cinema c WHERE c.slug = :slug")
    List<Movie> findByCinemaSlug(@Param("slug") String slug);

    Page<Movie> findByTitleContainingOrSummaryContaining(
            String titleKeyword, String summaryKeyword, Pageable pageable
    );



}

