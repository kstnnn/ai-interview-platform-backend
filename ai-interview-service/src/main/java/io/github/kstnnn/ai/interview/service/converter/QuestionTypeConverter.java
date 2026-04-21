package io.github.kstnnn.ai.interview.service.converter;

import io.github.kstnnn.ai.interview.service.model.QuestionType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class QuestionTypeConverter implements AttributeConverter<QuestionType, String> {

  @Override
  public String convertToDatabaseColumn(QuestionType attribute) {
    return attribute != null ? attribute.name() : null;
  }

  @Override
  public QuestionType convertToEntityAttribute(String dbData) {
    return dbData != null ? QuestionType.valueOf(dbData) : null;
  }
}
