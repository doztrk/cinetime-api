package com.Cinetime.repo;

import com.Cinetime.entity.Cinema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface CinemaRepository extends JpaRepository<Cinema,Long> {

    @Query("SELECT c FROM Cinema c " +
            "JOIN City city ON c.city.id = city.id " +
            "LEFT JOIN Hall h ON h.cinema.id = c.id " +
            "WHERE (:cityId IS NULL OR city.id = :cityId) " +  //cityhall ve speial null ise tum sinemalar gelir, optional old icin
            "AND (:specialHall IS NULL OR h.isSpecial = true) ")
    Page<Cinema> findCinemasByFilters(
            @Param("cityId") Long cityId,
            @Param("specialHall") Boolean specialHall,
            Pageable pageable
    );
}
