package com.Cinetime.Repository;

import com.Cinetime.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    // You can add custom queries here.
}