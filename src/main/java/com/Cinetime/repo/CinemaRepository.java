package com.Cinetime.repo;

import com.Cinetime.entity.Cinema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CinemaRepository extends JpaRepository<Cinema, Long> {

    @Query(value = "SELECT DISTINCT c.* FROM cinema c " +
            "WHERE (:cityId IS NULL OR c.city_id = :cityId) " +
            "AND (:specialHall IS NULL OR EXISTS (SELECT 1 FROM hall h WHERE h.cinema_id = c.id AND h.is_special = true AND LOWER(h.name) LIKE LOWER(:specialHall)))",
            nativeQuery = true)
    Page<Cinema> findCinemasByFilters(
            @Param("cityId") Long cityId,
            @Param("specialHall") String specialHall,
            Pageable pageable);


    @Query("SELECT DISTINCT c FROM Cinema c JOIN c.halls h JOIN h.showtimes s WHERE s.movie.id = :movieId")
    Page<Cinema> findCinemasByMovieId(@Param("movieId") Long movieId, Pageable pageable);

    Optional<Cinema> findBySlug(String slug);

    @Query("SELECT DISTINCT c FROM Cinema c JOIN c.halls h WHERE LOWER(h.name) LIKE LOWER(CONCAT('%', :hallName, '%'))")
    Page<Cinema> findCinemasByHallName(@Param("hallName") String hallName, Pageable pageable);
}
