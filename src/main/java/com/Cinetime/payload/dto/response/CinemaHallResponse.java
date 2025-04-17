package com.Cinetime.payload.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CinemaHallResponse {


    private Long cinemaId;
    private String cinemaName;
    private String address;
    private String city;
    private String district;
    private List<HallResponse> halls;
}
