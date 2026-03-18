package io.github.kstnnn.common.logging.json;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import io.github.kstnnn.common.logging.annotation.Sensitive;
import java.util.List;

public class SensitiveBeanSerializerModifier extends BeanSerializerModifier {

  @Override
  public List<BeanPropertyWriter> changeProperties(
      SerializationConfig config,
      BeanDescription beanDesc,
      List<BeanPropertyWriter> beanProperties) {

    for (BeanPropertyWriter writer : beanProperties) {
      if (writer.getMember().getAnnotated().getAnnotation(Sensitive.class) != null) {
        writer.assignSerializer(new SensitiveValueSerializer());
      }
    }

    return beanProperties;
  }
}
