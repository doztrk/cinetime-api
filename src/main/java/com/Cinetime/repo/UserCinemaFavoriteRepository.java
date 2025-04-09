package com.Cinetime.repo;

import com.Cinetime.entity.User;
import com.Cinetime.entity.UserCinemaFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCinemaFavoriteRepository extends JpaRepository<UserCinemaFavorite, Long> {

    Page<UserCinemaFavorite> findByUser(User user, Pageable pageable);
}
