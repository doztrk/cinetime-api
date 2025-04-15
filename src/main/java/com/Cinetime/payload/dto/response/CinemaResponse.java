package com.Cinetime.payload.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CinemaResponse {
    private Long id;
    private String name;
    private String slug;
    private String address;
    private String phone;
    private String email;
    private String district;
    private String city;

}
