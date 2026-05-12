package io.github.kstnnn.ai.interview.service.converter;

import io.github.kstnnn.ai.interview.service.model.InterviewLevel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class InterviewLevelConverter implements AttributeConverter<InterviewLevel, String> {

  @Override
  public String convertToDatabaseColumn(InterviewLevel attribute) {
    return attribute != null ? attribute.name() : null;
  }

  @Override
  public InterviewLevel convertToEntityAttribute(String dbData) {
    return dbData != null ? InterviewLevel.valueOf(dbData) : null;
  }
}
