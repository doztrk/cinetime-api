package com.Cinetime.repo;

import com.Cinetime.entity.Role;
import com.Cinetime.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByRoleName(RoleName roleName);
}
