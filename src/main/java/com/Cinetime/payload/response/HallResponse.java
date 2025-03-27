package com.Cinetime.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HallResponse {
    private Long id;
    private String name;
    private Integer seatCapacity;
    private Boolean isSpecial;
}