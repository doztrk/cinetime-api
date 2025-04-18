package com.Cinetime.payload.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketRequestDto {

        private Long movieId;
        private Long showtimeId;
        private String seatLetter;
        private Integer seatNumber;

    }

