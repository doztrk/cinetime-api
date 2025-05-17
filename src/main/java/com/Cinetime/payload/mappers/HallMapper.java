package com.Cinetime.payload.mappers;

import com.Cinetime.entity.Hall;
import com.Cinetime.payload.dto.response.HallResponse;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Component
public class HallMapper {

    public List<HallResponse> mapHallToHallResponse(List<Hall> halls) {
        if (halls == null || halls.isEmpty()) {
            return Collections.emptyList();
        }

        return halls.stream()
                .map(this::mapToHallResponse)
                .collect(Collectors.toList());
    }

    public HallResponse mapToHallResponse(Hall hall) {
        if (hall == null) {
            return null;
        }

        return HallResponse.builder()
                .id(hall.getId())
                .name(hall.getName())
                .seatCapacity(hall.getSeatCapacity())
                .isSpecial(hall.getIsSpecial() != null ? hall.getIsSpecial() : false)
                .build();
    }

    public Set<HallResponse> mapHallToHallResponse(Set<Hall> halls) {
        if (halls == null || halls.isEmpty()) {
            return Collections.emptySet();
        }

        return halls.stream()
                .map(this::mapToHallResponse)
                .collect(Collectors.toSet());
    }

}
