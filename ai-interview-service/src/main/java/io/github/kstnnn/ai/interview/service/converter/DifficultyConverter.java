package io.github.kstnnn.ai.interview.service.converter;

import io.github.kstnnn.ai.interview.service.model.Difficulty;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DifficultyConverter implements AttributeConverter<Difficulty, String> {

  @Override
  public String convertToDatabaseColumn(Difficulty attribute) {
    return attribute != null ? attribute.name() : null;
  }

  @Override
  public Difficulty convertToEntityAttribute(String dbData) {
    return dbData != null ? Difficulty.valueOf(dbData) : null;
  }
}
