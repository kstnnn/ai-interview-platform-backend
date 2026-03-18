package io.github.kstnnn.common.logging.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.kstnnn.common.logging.json.SensitiveBeanSerializerModifier;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class LoggingAspect {

  private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

  private static final ObjectMapper MAPPER = createObjectMapper();

  private static final String POINTCUT =
      "@annotation(com.diploma.common.logging.annotation.Loggable)";

  @Before(POINTCUT)
  public void logEntry(JoinPoint joinPoint) {
    var signature = joinPoint.getSignature();
    log.info(
        ">>> Enter: {}.{} with args: {}",
        signature.getDeclaringTypeName(),
        signature.getName(),
        toJson(joinPoint.getArgs()));
  }

  @AfterReturning(pointcut = POINTCUT, returning = "result")
  public void logExit(JoinPoint joinPoint, Object result) {
    var signature = joinPoint.getSignature();
    log.info(
        "<<< Exit: {}.{} with result: {}",
        signature.getDeclaringTypeName(),
        signature.getName(),
        toJson(result));
  }

  @AfterThrowing(pointcut = POINTCUT, throwing = "ex")
  public void logError(JoinPoint joinPoint, Throwable ex) {
    var signature = joinPoint.getSignature();
    log.error(
        "!!! Error in: {}.{} - {}",
        signature.getDeclaringTypeName(),
        signature.getName(),
        ex.getMessage(),
        ex);
  }

  private static ObjectMapper createObjectMapper() {
    var module = new SimpleModule();
    module.setSerializerModifier(new SensitiveBeanSerializerModifier());

    return JsonMapper.builder().addModule(module).build();
  }

  private static String toJson(Object obj) {
    if (obj == null) return "null";
    try {
      return MAPPER.writeValueAsString(obj);
    } catch (Exception e) {
      return "[SERIALIZE_ERROR:" + obj.getClass().getSimpleName() + "]";
    }
  }
}
