package com.Cinetime.payload.mappers;

import com.Cinetime.entity.Hall;
import com.Cinetime.payload.dto.response.HallResponse;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class HallMapper {

    public static HallResponse mapHallToHallResponse(Hall hall){
        return HallResponse.builder()
                .id(hall.getId())
                .name(hall.getName())
                .seatCapacity(hall.getSeatCapacity())
                .isSpecial(hall.getIsSpecial())
                .build();
    }
}
