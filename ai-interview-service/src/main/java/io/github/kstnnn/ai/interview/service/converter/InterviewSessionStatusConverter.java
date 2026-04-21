package io.github.kstnnn.ai.interview.service.converter;

import io.github.kstnnn.ai.interview.service.model.InterviewSessionStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class InterviewSessionStatusConverter
    implements AttributeConverter<InterviewSessionStatus, String> {

  @Override
  public String convertToDatabaseColumn(InterviewSessionStatus attribute) {
    return attribute != null ? attribute.name() : null;
  }

  @Override
  public InterviewSessionStatus convertToEntityAttribute(String dbData) {
    return dbData != null ? InterviewSessionStatus.valueOf(dbData) : null;
  }
}
