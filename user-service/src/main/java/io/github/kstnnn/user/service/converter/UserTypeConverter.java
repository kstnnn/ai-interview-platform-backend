package io.github.kstnnn.user.service.converter;

import io.github.kstnnn.user.service.enums.UserType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserTypeConverter implements AttributeConverter<UserType, String> {
  @Override
  public String convertToDatabaseColumn(UserType attribute) {
    return attribute != null ? attribute.name() : null;
  }

  @Override
  public UserType convertToEntityAttribute(String dbData) {
    return dbData != null ? UserType.valueOf(dbData) : null;
  }
}
