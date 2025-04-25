package com.Cinetime.service;

import com.Cinetime.entity.Cinema;
import com.Cinetime.entity.Hall;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.payload.dto.response.CinemaHallResponse;
import com.Cinetime.payload.dto.response.HallResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.mappers.CinemaHallMapper;
import com.Cinetime.payload.mappers.HallMapper;
import com.Cinetime.repo.CinemaRepository;
import com.Cinetime.repo.HallRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CinemaService {

    private final CinemaRepository cinemaRepository;
    private final PageableHelper pageableHelper;
    private final HallMapper hallMapper;
    private final CinemaHallMapper cinemaHallMapper;


    //C01
    public ResponseMessage<List<Cinema>> getCinemasByFilters(Long cityId, String specialHallName, int page, int size, String sort, String type) {


        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);
        String formattedSpecialHall = (specialHallName != null) ? "%" + specialHallName + "%" : null;

        Page<Cinema> cinemasPage = cinemaRepository.findCinemasByFilters(cityId, formattedSpecialHall, pageable);


        List<Cinema> cinemas = cinemasPage.getContent();

        if (cinemas.isEmpty()) {
            return ResponseMessage.<List<Cinema>>builder()
                    .message("No cinemas found")
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        return ResponseMessage.<List<Cinema>>builder()
                .httpStatus(HttpStatus.OK)
                .object(cinemas)
                .build();
    }


    //C03 return cinema details by id
    public ResponseMessage<Cinema> getCinemaById(Long id) {

        Optional<Cinema> cinema = cinemaRepository.findById(id);

        if (cinema.isEmpty()) {
            return ResponseMessage.<Cinema>builder()
                    .message("Cinema not found")
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        return ResponseMessage.<Cinema>builder()
                .message("Cinema found")
                .httpStatus(HttpStatus.OK)
                .object(cinema.get())
                .build();
    }

    //C04
    @Transactional
    public ResponseMessage<CinemaHallResponse> getHallsByCinemaId(Long cinemaId) {

        Optional<Cinema> cinemaOptional = cinemaRepository.findById(cinemaId);

        if (cinemaOptional.isEmpty()) {
            return ResponseMessage.<CinemaHallResponse>builder()
                    .message("Cinema not found")
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }
        Cinema cinema = cinemaOptional.get();

        List<Hall> halls = cinema.getHalls() != null ? cinema.getHalls() : Collections.emptyList();

        List<HallResponse> hallResponses = hallMapper.mapHallToHallResponse(halls);

        CinemaHallResponse cinemaHallResponse = cinemaHallMapper.mapToCinemaHallResponse(cinema, hallResponses);

        return ResponseMessage.<CinemaHallResponse>builder()
                .message("Cinema halls found successfully")
                .httpStatus(HttpStatus.OK)
                .object(cinemaHallResponse)
                .build();
    }
}

