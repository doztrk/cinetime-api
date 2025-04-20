package com.Cinetime.payload.mappers;

import com.Cinetime.entity.Cinema;
import com.Cinetime.payload.dto.response.CinemaHallResponse;
import com.Cinetime.payload.dto.response.HallResponse;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Data
@Component
public class CinemaHallMapper {

    public CinemaHallResponse mapToCinemaHallResponse(Cinema cinema, List<HallResponse> hallResponses) {
        if (cinema == null) {
            return null;
        }

        return CinemaHallResponse.builder()
                .cinemaId(cinema.getId())
                .cinemaName(cinema.getName())
                .address(cinema.getAddress())  // Adding more cinema details could be useful
                .city(cinema.getCity() != null ? cinema.getCity().getName() : null)
                .district(cinema.getDistrict() != null ? cinema.getDistrict().getName() : null)
                .halls(hallResponses != null ? hallResponses : Collections.emptyList())
                .build();
    }
}
