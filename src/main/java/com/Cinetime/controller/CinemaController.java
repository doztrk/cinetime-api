package com.Cinetime.controller;

import com.Cinetime.entity.Cinema;
import com.Cinetime.payload.dto.response.CinemaHallResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.service.CinemaService;
import com.Cinetime.service.HallService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cinemas")
@RequiredArgsConstructor
public class CinemaController {

    private final CinemaService cinemaService;
    private final HallService hallService;

    //C01
    @GetMapping
    public ResponseMessage<List<Cinema>> getCinemas(
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) String specialHall,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String type
    ) {
        return cinemaService.getCinemasByFilters(cityId, specialHall, page, size, sort, type);

    }


    //C03 return cinema details by id
    @GetMapping("/{id}")
    public ResponseMessage<Cinema> getCinemaDetails(@PathVariable Long id) {
        return cinemaService.getCinemaById(id);
    }

    //C04 return cinema's halls by id
    @GetMapping("/{cinemaId}/halls")
    public ResponseMessage<CinemaHallResponse> getCinemaHalls(@PathVariable Long cinemaId) {
        return cinemaService.getHallsByCinemaId(cinemaId);
    }









}
