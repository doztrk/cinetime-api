package com.Cinetime.security;


import com.Cinetime.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom implementation of UserDetails that adapts our User entity
 * to Spring Security's authentication system.
 */
public class UserDetailsImpl implements UserDetails {
    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(User user) {
        this.user = user;
        String roleName = user.getRole() != null && user.getRole().getRoleName() != null
                ? "ROLE_" + user.getRole().getRoleName().name()
                : "ROLE_ANONYMOUS";
        this.authorities = Collections.singleton(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }


    @Override
    public String getUsername() {
        return user.getPhoneNumber();  // Return phone number instead of email and we are going to use phoneNumber as username credentials
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Get the underlying user entity
     */
    public User getUser() {
        return user;
    }
}