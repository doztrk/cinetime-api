package com.Cinetime.payload.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

import java.time.LocalDateTime;
@Data
@Builder
public class ShowtimeResponse {
    private Long id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private MovieResponse movie;
    private HallResponse hall;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}