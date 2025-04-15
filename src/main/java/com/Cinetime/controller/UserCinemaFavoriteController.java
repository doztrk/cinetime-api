package com.Cinetime.controller;

import com.Cinetime.entity.Cinema;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.service.UserCinemaFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class UserCinemaFavoriteController {

    private final UserCinemaFavoriteService userCinemaFavoriteService;


    @GetMapping("/auth")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseMessage<List<Cinema>> getFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String type
    ) {
        return userCinemaFavoriteService.getUserFavoriteCinemas(page, size, sort, type);
    }


}
