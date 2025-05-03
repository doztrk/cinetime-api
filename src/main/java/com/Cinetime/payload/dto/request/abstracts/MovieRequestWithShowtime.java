package com.Cinetime.payload.dto.request.abstracts;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieRequestWithShowtime extends AbstractMovieRequest{

    @NotNull(message = "Please provide a showtime id")
    private Long showtimeId;

}
