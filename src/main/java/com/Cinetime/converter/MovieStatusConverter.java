package com.Cinetime.converter;

import com.Cinetime.enums.MovieStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class MovieStatusConverter implements AttributeConverter<MovieStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(MovieStatus status) {
        return status.getValue();
    }

    @Override
    public MovieStatus convertToEntityAttribute(Integer value) {
        return MovieStatus.fromValue(value);
    }


}
