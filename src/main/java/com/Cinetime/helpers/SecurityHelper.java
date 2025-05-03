package com.Cinetime.helpers;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Optional;

public class SecurityHelper {


    /**
     * Get the phone number of the currently authenticated user
     *
     * @return the phone number or null if not authenticated
     */

    public static String getCurrentUserPhoneNumber() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    /**
     * Get all authorities of the currently authenticated user
     *
     * @return collection of authorities or empty collection if not authenticated
     */
    public static Collection<? extends GrantedAuthority> getCurrentUserAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getAuthorities() : java.util.Collections.emptyList();
    }

    /**
     * Check if the current user has a specific role
     *
     * @param role the role to check (without ROLE_ prefix)
     * @return true if the user has the role, false otherwise
     */
    public static boolean hasRole(String role) {
        String roleWithPrefix = "ROLE_" + role;
        return getCurrentUserAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(roleWithPrefix));
    }

    /**
     * Get the role name of the current user (without ROLE_ prefix)
     *
     * @return the role name or empty if not found
     */
    public static Optional<String> getCurrentUserRole() {
        return getCurrentUserAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role);
    }

    /**
     * Check if the current user is authenticated
     *
     * @return true if authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal());
    }


}