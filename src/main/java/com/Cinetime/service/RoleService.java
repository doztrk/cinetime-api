package com.Cinetime.service;

import com.Cinetime.entity.Role;
import com.Cinetime.enums.RoleName;
import com.Cinetime.repo.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoleService {


    //Role Cacheleme

    private final RoleRepository roleRepository;
    private final Map<RoleName, Role> roleCache = new EnumMap<>(RoleName.class);

    @PostConstruct //Dependency injection yapilip bean devreye girmeden once bu kodu calistiriyor.
    public void init() {
        roleRepository.findAll().forEach(role -> roleCache.put(role.getRoleName(), role));
        for (RoleName roleName : RoleName.values()) {
            if (!roleCache.containsKey(roleName)) {
                Role newRole = new Role(roleName);
                Role savedRole = roleRepository.save(newRole);
                roleCache.put(roleName, savedRole);
            }
        }
    }

    public Role getRole(RoleName roleName) {
        return roleCache.get(roleName);
    }

}
