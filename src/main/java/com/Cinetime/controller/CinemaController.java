package com.Cinetime.controller;

import com.Cinetime.entity.Cinema;
import com.Cinetime.payload.response.CinemaResponse;
import com.Cinetime.payload.response.HallResponse;
import com.Cinetime.service.CinemaService;
import com.Cinetime.service.authentication.HallService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CinemaController {

    private final CinemaService cinemaService;
    private final HallService hallService;

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

    //C03 return cinema details by id

    @GetMapping("/{id}")
    public CinemaResponse getCinemaDetails(@PathVariable Long id) {
        return cinemaService.getCinemaById(id);
    }

    //C04 return cinema's halls by id

    @GetMapping("/{cinemaId}/halls")
    public List<HallResponse> getCinemaHalls(@PathVariable Long cinemaId) {
        return hallService.getHallsByCinemaId(cinemaId);
    }

    //C05 return all of special halls

    @GetMapping("/special-halls")
    public List<HallResponse> getSpecialHalls() {
        return hallService.getSpecialHalls();
    }





}
