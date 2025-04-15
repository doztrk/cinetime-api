package com.Cinetime.repo;

import com.Cinetime.entity.Cinema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface CinemaRepository extends JpaRepository<Cinema, Long> {

    @Query("SELECT DISTINCT c FROM Cinema c " +
            "WHERE (:cityId IS NULL OR c.city.id = :cityId) " +
            "AND (:specialHall IS NULL OR EXISTS (SELECT h FROM Hall h WHERE h.cinema = c AND LOWER(h.name) LIKE LOWER(CONCAT('%', :specialHall, '%'))))")
    Page<Cinema> findCinemasByFilters(
            @Param("cityId") Long cityId,
            @Param("specialHall") String specialHall,
            Pageable pageable);
}
