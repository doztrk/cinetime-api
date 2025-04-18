package com.Cinetime.controller;

import com.Cinetime.payload.dto.response.HallResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.service.HallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/halls")
@RequiredArgsConstructor
@Tag(name = "Hall Management", description = "APIs for managing cinema halls")
public class HallController {

    private final HallService hallService;


    //C05
    @Operation(
            summary = "Get All Special Halls",
            description = "Returns a list of all special halls across all cinemas"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved special halls list",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "401", description = "Not authorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/special-halls")
    public ResponseMessage<List<HallResponse>> getAllSpecialHalls() {
        return hallService.getAllSpecialHalls();
    }


}