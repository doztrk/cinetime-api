package com.Cinetime.helpers;

import com.Cinetime.entity.Hall;
import com.Cinetime.entity.Movie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class TicketPriceHelper {

    private static final double BASE_PRICE = 200.0;
    private static final double SPECIAL_HALL_MULTIPLIER = 1.3;
    private static final double WEEKEND_MULTIPLIER = 1.5;
    private static final double NO_MULTIPLIER = 1.0;

    public Double calculateTicketPrice(Hall hall, Movie movie, LocalTime startTime, LocalTime endTime, LocalDate date) {


        double hallMultiplier = Boolean.TRUE.equals(hall.getIsSpecial()) ? SPECIAL_HALL_MULTIPLIER : NO_MULTIPLIER;
        String dayOfWeek = determineWeekday(date);
        double weekendMultiplier = dayOfWeek.equals("WEEKEND") ? WEEKEND_MULTIPLIER : NO_MULTIPLIER;


        return BASE_PRICE * hallMultiplier * weekendMultiplier;
    }


    private String determineWeekday(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return "WEEKEND";
        }
        return "WEEKDAY";
    }
}
