package com.Cinetime.repo;

import com.Cinetime.entity.Hall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HallRepository extends JpaRepository<Hall, Long> {

    List<Hall> findByIsSpecialTrue();

}
