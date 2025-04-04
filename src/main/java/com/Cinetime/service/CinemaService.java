package com.Cinetime.service;

import com.Cinetime.entity.Cinema;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.payload.mappers.CinemaMapper;
import com.Cinetime.payload.response.CinemaResponse;
import com.Cinetime.repo.CinemaRepository;
import com.Cinetime.repo.UserCinemaFavoriteRepository;
import com.Cinetime.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CinemaService {

    private final CinemaRepository cinemaRepository;
    private final UserRepository userRepository;
    private final PageableHelper pageableHelper;
    private final UserCinemaFavoriteRepository userCinemaFavoriteRepository;
    private final CinemaMapper cinemaMapper;

    //C01
    public ResponseEntity<Page<Cinema>>  getCinemasByFilters(Long cityId, Boolean specialHall, int page, int size, String sort, String type) {
        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);

        Page<Cinema> cinemas = cinemaRepository.findCinemasByFilters(cityId, specialHall, pageable);


        // Eğer hiç sonuç yoksa, boş sayfa döner
        if (cinemas.isEmpty()) {
            return ResponseEntity.ok(
                    new PageImpl<Cinema>(Collections.emptyList(), pageable, 0)
            );
        }

        return ResponseEntity.ok(cinemas);
    }


    //C03 return cinema details by id
    public CinemaResponse getCinemaById(Long id) {
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cinema not found"));

        return cinemaMapper.mapCinemaToCinemaResponse(cinema); //Todo: Hall listesini, seansları ya da favorilere eklenip eklenmediği listelenebilir
    }
}

