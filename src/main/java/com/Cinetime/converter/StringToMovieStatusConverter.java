package com.Cinetime.converter;

import com.Cinetime.enums.MovieStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToMovieStatusConverter implements Converter<String, MovieStatus> {
    @Override
    public MovieStatus convert(String source) {
        try {
            int value = Integer.parseInt(source);
            return MovieStatus.fromValue(value);
        } catch (NumberFormatException e) {
            return MovieStatus.valueOf(source);
        }
    }
}