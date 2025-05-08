package com.Cinetime.payload.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShowtimeRequest {

    @Future(message = "Showtime Date cannot be in the past")
    @NotNull(message = "Showtime Date cannot be empty")
    private LocalDate date;

    @NotNull(message = "Showtime Start Time cannot be empty")
    private LocalTime startTime;

    @NotNull(message = "Showtime Start Time cannot be empty")
    private LocalTime endTime;

    @NotNull(message = "ID of the movie you are trying to set showtime for cannot be empty")
    private Long movieId;

    @NotNull(message = "Hall ID of Showtime cannot be empty")
    private Long hallId;


}
