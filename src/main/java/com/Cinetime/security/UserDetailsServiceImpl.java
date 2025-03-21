package com.Cinetime.security;

import com.Cinetime.entity.User;
import com.Cinetime.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

/**
 * Implementation of Spring Security's UserDetailsService.
 * Loads user-specific data and builds a UserDetails object.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Load a user by email and convert to Spring Security's UserDetails.
     * This method is used by Spring Security during authentication.
     *
     * @param phoneNumber The user's phoneNumber (used as username)
     * @return UserDetails object populated with user information
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        logger.debug("Loading user by phone number: {}", phoneNumber);

        User user = userRepository.findByPhoneNumber(phoneNumber).orElseThrow(
                () -> new UsernameNotFoundException("User not found with phone number: " + phoneNumber));


        logger.debug("User found with phone number: {}", phoneNumber);

        return new UserDetailsImpl(user);
    }
}