package io.github.kstnnn.ai.interview.service.service;

import io.github.kstnnn.ai.interview.service.dto.VoiceTranscriptionResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface VoiceService {
  VoiceTranscriptionResponseDto transcribe(MultipartFile audio, String initialPrompt);

  byte[] synthesize(String text, String speaker);
}
