package com.Cinetime.repo;

import com.Cinetime.entity.Movie;
import com.Cinetime.enums.MovieStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByStatus(MovieStatus status, Pageable pageable);

    boolean existsBySlug(String slug);
}

