package com.Cinetime.payload.mappers;

import com.Cinetime.entity.Cinema;
import com.Cinetime.payload.response.CinemaResponse;
import org.springframework.stereotype.Component;

@Component
public class CinemaMapper {
    public CinemaResponse mapCinemaToCinemaResponse(Cinema cinema) {

        return CinemaResponse.builder()
                .id(cinema.getId())
                .name(cinema.getName())
                .slug(cinema.getSlug())
                .address(cinema.getAddress())
                .phone(cinema.getPhone())
                .email(cinema.getEmail())
                .district(cinema.getDistrict().getName())
                .city(cinema.getCity().getName())
                .build();
    }
}
