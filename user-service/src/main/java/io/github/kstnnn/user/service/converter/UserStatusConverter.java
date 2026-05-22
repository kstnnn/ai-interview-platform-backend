package io.github.kstnnn.user.service.converter;

import io.github.kstnnn.user.service.model.UserStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserStatusConverter implements AttributeConverter<UserStatus, String> {
  @Override
  public String convertToDatabaseColumn(UserStatus attribute) {
    return attribute != null ? attribute.name() : null;
  }

  @Override
  public UserStatus convertToEntityAttribute(String dbData) {
    return dbData != null ? UserStatus.valueOf(dbData) : null;
  }
}
