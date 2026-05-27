package io.github.kstnnn.ai.interview.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AiInterviewServiceApplication {

  public static void main(String[] args) {
    System.setProperty("ai.djl.deviceType", "cpu");
    System.setProperty("pytorch_flavor", "cpu");
    SpringApplication.run(AiInterviewServiceApplication.class, args);
  }
}
