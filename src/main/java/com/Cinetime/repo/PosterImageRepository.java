package com.Cinetime.repo;

import com.Cinetime.entity.PosterImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PosterImageRepository extends JpaRepository<PosterImage, Long> {
}
