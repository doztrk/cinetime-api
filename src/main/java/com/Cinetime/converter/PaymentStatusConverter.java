package com.Cinetime.converter;

import com.Cinetime.enums.PaymentStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PaymentStatusConverter implements AttributeConverter<PaymentStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PaymentStatus status) {
        return status.getValue();
    }

    @Override
    public PaymentStatus convertToEntityAttribute(Integer value) {
        return PaymentStatus.fromValue(value);
    }
}
