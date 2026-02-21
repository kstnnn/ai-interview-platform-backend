package io.github.kstnnn.user.service.converter

import io.github.kstnnn.user.service.enums.UserType
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class UserTypeConverter : AttributeConverter<UserType, String> {
    override fun convertToDatabaseColumn(attribute: UserType?): String? = attribute?.name

    override fun convertToEntityAttribute(dbData: String?): UserType? = dbData?.let { UserType.valueOf(it) }
}
