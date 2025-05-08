package com.Cinetime.controller;

import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.dto.response.SeatResponse;
import com.Cinetime.service.SeatService;
import com.Cinetime.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seat")
public class SeatController {

    private final SeatService seatService;

    @Operation(
            summary = "Get All Occupied Seats for a Showtime",
            description = "Returns a complete list of all reserved seats for a specific showtime without pagination."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved reserved seats",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "Showtime not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/getOccupiedSeats/{showtimeId}")
    //TODO: Bu endpointi bilen herkes bu backende istek gonderebilir. Frontendden bu istek yapil
    public ResponseMessage<List<SeatResponse>> getOccupiedSeats(
            @Parameter(description = "ID of the showtime to get reserved seats for", required = true)
            @PathVariable Long showtimeId
    ) {
        return seatService.getOccupiedSeats(showtimeId);
    }

}
