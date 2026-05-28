package io.github.kstnnn.ai.interview.service.service.impl;

import io.github.kstnnn.ai.interview.service.dto.VoiceSynthesisRequestDto;
import io.github.kstnnn.ai.interview.service.dto.VoiceTranscriptionResponseDto;
import io.github.kstnnn.ai.interview.service.service.VoiceService;
import java.io.IOException;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class VoiceServiceImpl implements VoiceService {

  private final RestClient restClient;

  public VoiceServiceImpl(
      RestClient.Builder restClientBuilder,
      @Value("${app.voice-service.base-url:http://localhost:8090}") String voiceServiceBaseUrl,
      @Value("${app.voice-service.connect-timeout:10s}") Duration connectTimeout,
      @Value("${app.voice-service.read-timeout:180s}") Duration readTimeout) {
    this.restClient =
        restClientBuilder
            .baseUrl(voiceServiceBaseUrl)
            .requestFactory(requestFactory(connectTimeout, readTimeout))
            .build();
  }

  @Override
  public VoiceTranscriptionResponseDto transcribe(MultipartFile audio, String initialPrompt) {
    try {
      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
      body.add("audio", audioResource(audio));
      if (initialPrompt != null && !initialPrompt.isBlank()) {
        body.add("initial_prompt", initialPrompt);
      }

      return restClient
          .post()
          .uri("/api/v1/voice/transcribe")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .body(body)
          .retrieve()
          .body(VoiceTranscriptionResponseDto.class);
    } catch (IOException ex) {
      throw new IllegalArgumentException("Failed to read uploaded audio", ex);
    }
  }

  @Override
  public byte[] synthesize(String text, String speaker) {
    return restClient
        .post()
        .uri("/api/v1/voice/synthesize")
        .contentType(MediaType.APPLICATION_JSON)
        .body(new VoiceSynthesisRequestDto(text, speaker))
        .retrieve()
        .body(byte[].class);
  }

  private ByteArrayResource audioResource(MultipartFile audio) throws IOException {
    var filename = audio.getOriginalFilename() != null ? audio.getOriginalFilename() : "audio.webm";
    return new ByteArrayResource(audio.getBytes()) {
      @Override
      public String getFilename() {
        return filename;
      }
    };
  }

  private SimpleClientHttpRequestFactory requestFactory(
      Duration connectTimeout, Duration readTimeout) {
    var factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(connectTimeout);
    factory.setReadTimeout(readTimeout);
    return factory;
  }
}
