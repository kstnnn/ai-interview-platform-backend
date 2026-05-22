package io.github.kstnnn.ai.interview.service.converter;

import io.github.kstnnn.ai.interview.service.model.SelectionReason;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SelectionReasonConverter implements AttributeConverter<SelectionReason, String> {

  @Override
  public String convertToDatabaseColumn(SelectionReason attribute) {
    return attribute != null ? attribute.name() : null;
  }

  @Override
  public SelectionReason convertToEntityAttribute(String dbData) {
    return dbData != null ? SelectionReason.valueOf(dbData) : null;
  }
}
