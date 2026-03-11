package io.github.kstnnn.user.service.converter;

import io.github.kstnnn.user.service.enums.UserRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

  @Override
  public String convertToDatabaseColumn(UserRole attribute) {
    return attribute != null ? attribute.name() : null;
  }

  @Override
  public UserRole convertToEntityAttribute(String dbData) {
    return dbData != null ? UserRole.valueOf(dbData) : null;
  }
}
