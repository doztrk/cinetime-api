package com.Cinetime.service;

import com.Cinetime.enums.TicketStatus;

import com.Cinetime.payload.business.SeatInfo;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.dto.response.SeatResponse;
import com.Cinetime.payload.mappers.SeatMapper;
import com.Cinetime.repo.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SeatService {

    private final TicketRepository ticketRepository;
    private final SeatMapper seatMapper;

    public ResponseMessage<List<SeatResponse>> getOccupiedSeats(Long showtimeId) {
        // Fetch all reserved seats for the showtime
        List<SeatInfo> reservedSeats = ticketRepository.findOccupiedSeatInfoByShowtimeAndStatus(
                showtimeId,
                List.of(TicketStatus.PAID, TicketStatus.RESERVED)
        );

        // Map to response objects
        List<SeatResponse> seatResponses = seatMapper.mapSeatInfoListToSeatResponseList(reservedSeats);

        return ResponseMessage.<List<SeatResponse>>builder()
                .message("Reserved seats found successfully")
                .object(seatResponses)
                .httpStatus(HttpStatus.OK)
                .build();
    }
}
