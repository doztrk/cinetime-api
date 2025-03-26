package com.Cinetime.controller;

import com.Cinetime.entity.Cinema;
import com.Cinetime.payload.response.CinemaResponse;
import com.Cinetime.service.CinemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CinemaController {

    private final CinemaService cinemaService;

    //C01
    @GetMapping("/cinemas")
    public ResponseEntity<Page<Cinema>> getCinemas(
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Boolean specialHall,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String type
    ) {
        Page<Cinema> cinemas = cinemaService.getCinemasByFilters(cityId, specialHall, page, size, sort, type);


        return ResponseEntity.ok(cinemas);
    }

    //C02 return cinemas based on user's favorites
    @PreAuthorize("hasAnyAuthority('MEMBER')")
    @GetMapping("/auth")
    public Page<CinemaResponse>getUserFavoriteCinemas(
            @RequestParam(value = "page",defaultValue = "0") int page,
            @RequestParam(value = "size",defaultValue = "10") int size,
            @RequestParam(value = "sort",defaultValue = "lessonName") String sort,
            @RequestParam(value = "type",defaultValue = "desc") String type,
            Principal principal  //giriş yapan (authenticated) kullanıcının kimliğini temsil eder.
    ){
        return cinemaService.getUserFavoriteCinemas(page,size,sort,type,principal);
    }





}
