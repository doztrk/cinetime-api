package com.Cinetime.service;

import com.Cinetime.entity.Cinema;
import com.Cinetime.entity.Hall;
import com.Cinetime.payload.mappers.HallMapper;
import com.Cinetime.payload.dto.response.HallResponse;
import com.Cinetime.repo.CinemaRepository;
import com.Cinetime.repo.HallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class HallService {
    private final CinemaRepository cinemaRepository;
    private final HallMapper hallMapper;
    private final HallRepository hallRepository;

    //C04 return cinema's halls by id
    public List<HallResponse> getHallsByCinemaId(Long cinemaId) {
        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new RuntimeException("Cinema not found"));

        List<Hall> halls = cinema.getHalls();

        return halls
                .stream()
                .map(HallMapper::mapHallToHallResponse)
                .toList();
    }

    //C05 return all of special halls
    /*
    public List<HallResponse> getSpecialHalls() {
        return hallRepository.findByIsSpecialTrue()
                .stream()
                .map(HallMapper::mapHallToHallResponse)
                .toList();
    }*/
}
