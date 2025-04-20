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

    @Query(value = "SELECT DISTINCT c.* FROM cinema c " +
            "WHERE (:cityId IS NULL OR c.city_id = :cityId) " +
            "AND (:specialHall IS NULL OR EXISTS (SELECT h.id FROM hall h WHERE h.cinema_id = c.id AND LOWER(CAST(h.name AS text)) LIKE LOWER(CONCAT('%', :specialHall, '%'))))",
            nativeQuery = true)
    Page<Cinema> findCinemasByFilters(
            @Param("cityId") Long cityId,
            @Param("specialHall") String specialHall,
            Pageable pageable);
}
