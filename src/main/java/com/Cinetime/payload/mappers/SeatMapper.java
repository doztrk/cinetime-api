package com.Cinetime.payload.mappers;

import com.Cinetime.payload.business.SeatInfo;
import com.Cinetime.payload.dto.response.SeatResponse;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@Data
public class SeatMapper {

    public SeatResponse mapSeatInfoToSeatResponse(SeatInfo seatInfo) {
        return SeatResponse.builder()
                .seatLetter(seatInfo.getSeatLetter())
                .seatNumber(seatInfo.getSeatNumber())
                .price(seatInfo.getPrice())
                .fullSeatName(seatInfo.getFullSeatName())
                .build();
    }

    public List<SeatResponse> mapSeatInfoListToSeatResponseList(List<SeatInfo> seatInfoList) {
        return seatInfoList.stream()
                .map(this::mapSeatInfoToSeatResponse)
                .toList();
    }
}
