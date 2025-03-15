package com.Cinetime.converter;


import com.Cinetime.enums.TicketStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TicketStatusConverter implements AttributeConverter<TicketStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TicketStatus status) {
        return status.getValue();
    }

    @Override
    public TicketStatus convertToEntityAttribute(Integer value) {
        return TicketStatus.fromValue(value);
    }
}
