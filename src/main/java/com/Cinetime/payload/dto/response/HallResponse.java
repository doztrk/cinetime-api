package com.Cinetime.payload.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class HallResponse {
    private Long id;
    private String name;
    private Integer seatCapacity;
    private Boolean isSpecial;

    // Parametreli constructor: id, name, seatCapacity ve isSpecial parametrelerini alır
    public HallResponse(Long id, String name, Integer seatCapacity, Boolean isSpecial) {
        this.id = id;
        this.name = name;
        this.seatCapacity = seatCapacity;
        this.isSpecial = isSpecial;
    }
}