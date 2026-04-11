package io.github.kstnnn.common.util;

public final class MaskingUtils {

  public static String mask(String value) {
    if (value == null || value.isEmpty()) {
      return value;
    }
    int maskedLength = value.length() / 2;
    String asterisks = "*".repeat(maskedLength);
    return asterisks + value.substring(maskedLength);
  }
}
