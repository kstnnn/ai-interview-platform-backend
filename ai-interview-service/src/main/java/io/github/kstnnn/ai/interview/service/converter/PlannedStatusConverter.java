package io.github.kstnnn.ai.interview.service.converter;

import io.github.kstnnn.ai.interview.service.model.PlannedStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PlannedStatusConverter implements AttributeConverter<PlannedStatus, String> {

  @Override
  public String convertToDatabaseColumn(PlannedStatus attribute) {
    return attribute != null ? attribute.name() : null;
  }

  @Override
  public PlannedStatus convertToEntityAttribute(String dbData) {
    return dbData != null ? PlannedStatus.valueOf(dbData) : null;
  }
}
