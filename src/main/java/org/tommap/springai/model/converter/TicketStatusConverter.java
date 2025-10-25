package org.tommap.springai.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.tommap.springai.model.entity.TicketStatus;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class TicketStatusConverter implements AttributeConverter<TicketStatus, String> {
    @Override
    public String convertToDatabaseColumn(TicketStatus attribute) {
        if (null == attribute) {
            return null;
        }

        return attribute.getValue();
    }

    @Override
    public TicketStatus convertToEntityAttribute(String dbData) {
        if (null == dbData) {
            return null;
        }

        return Stream.of(TicketStatus.values())
                .filter(status -> status.getValue().equals(dbData))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid ticket status!"));
    }
}
