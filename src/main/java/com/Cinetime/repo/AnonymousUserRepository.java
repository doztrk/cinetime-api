package com.Cinetime.repo;

import com.Cinetime.entity.AnonymousUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnonymousUserRepository extends JpaRepository<AnonymousUser,Long> {
}
