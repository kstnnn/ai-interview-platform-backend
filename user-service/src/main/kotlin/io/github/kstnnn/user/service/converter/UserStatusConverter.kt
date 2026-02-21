package io.github.kstnnn.user.service.converter

import io.github.kstnnn.user.service.enums.UserStatus
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class UserStatusConverter : AttributeConverter<UserStatus, String> {
    override fun convertToDatabaseColumn(attribute: UserStatus?): String? = attribute?.name

    override fun convertToEntityAttribute(dbData: String?): UserStatus? = dbData?.let { UserStatus.valueOf(it) }
}
