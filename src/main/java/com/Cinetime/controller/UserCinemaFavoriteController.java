package com.Cinetime.controller;

import com.Cinetime.entity.Cinema;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.service.UserCinemaFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Tag(name = "User Favorites", description = "APIs for managing user favorite cinemas")
public class UserCinemaFavoriteController {

    private final UserCinemaFavoriteService userCinemaFavoriteService;

    //C02
    @Operation(
            summary = "Get User's Favorite Cinemas {C02}",
            description = "Returns a list of cinemas that the authenticated user has marked as favorites. Requires MEMBER role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved favorite cinemas list",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires MEMBER role"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/auth")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseMessage<List<Cinema>> getFavorites(
            @Parameter(description = "Page number (zero-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of records per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "name") String sort,
            @Parameter(description = "Sort direction (asc or desc)") @RequestParam(defaultValue = "asc") String type
    ) {
        return userCinemaFavoriteService.getUserFavoriteCinemas(page, size, sort, type);
    }
}