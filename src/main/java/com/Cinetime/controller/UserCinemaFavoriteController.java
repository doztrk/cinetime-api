package com.Cinetime.controller;

import com.Cinetime.entity.User;
import com.Cinetime.payload.response.CinemaResponse;
import com.Cinetime.security.UserDetailsImpl;
import com.Cinetime.service.UserCinemaFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class UserCinemaFavoriteController {
    private final UserCinemaFavoriteService favoriteService;


    @GetMapping("/auth")
    public ResponseEntity<Page<CinemaResponse>> getUserFavoriteCinemas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String type,
            Authentication authentication
    ) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();

        Page<CinemaResponse> response = favoriteService.getUserFavoriteCinemas(user, page, size, sort, type);
        return ResponseEntity.ok(response);
    }
}
