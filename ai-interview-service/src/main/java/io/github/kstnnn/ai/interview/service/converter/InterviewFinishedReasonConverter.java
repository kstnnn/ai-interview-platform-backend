package io.github.kstnnn.ai.interview.service.converter;

import io.github.kstnnn.ai.interview.service.model.InterviewFinishedReason;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class InterviewFinishedReasonConverter
    implements AttributeConverter<InterviewFinishedReason, String> {

  @Override
  public String convertToDatabaseColumn(InterviewFinishedReason attribute) {
    return attribute != null ? attribute.name() : null;
  }

  @Override
  public InterviewFinishedReason convertToEntityAttribute(String dbData) {
    return dbData != null ? InterviewFinishedReason.valueOf(dbData) : null;
  }
}
