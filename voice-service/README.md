## Voice Service

Local STT/TTS service for the AI interview platform.

### Run

```bash
uv run uvicorn app.main:app --host 0.0.0.0 --port 8090 --reload
```

Or use the project script:

```bash
uv run voice-service
```

### Health

```bash
curl http://localhost:8090/health
```

### Transcribe

```bash
curl -F "audio=@sample.wav" http://localhost:8090/api/v1/voice/transcribe
```

For `curl`, it is safer to pass an explicit MIME type:

```bash
curl -F "audio=@sample.wav;type=audio/wav" http://localhost:8090/api/v1/voice/transcribe
curl -F "audio=@sample.mp3;type=audio/mpeg" http://localhost:8090/api/v1/voice/transcribe
```

You can pass interview-specific speech recognition context:

```bash
curl -F "audio=@sample.wav;type=audio/wav" \
  -F "initial_prompt=Техническое интервью. Возможные термины: Java, Spring, PostgreSQL, JPA, Hibernate." \
  http://localhost:8090/api/v1/voice/transcribe
```

Supported extensions: `.wav`, `.mp3`, `.m4a`, `.webm`, `.ogg`, `.flac`.

### Synthesize

```bash
curl -X POST http://localhost:8090/api/v1/voice/synthesize \
  -H "Content-Type: application/json" \
  -d '{"text":"Привет, начнем техническое интервью."}' \
  --output tts.wav
```

### STT Config

Environment variables use the `VOICE_` prefix:

```env
VOICE_STT_MODEL_SIZE=medium
VOICE_STT_DEVICE=cpu
VOICE_STT_COMPUTE_TYPE=int8
VOICE_STT_LANGUAGE=ru
VOICE_STT_INITIAL_PROMPT=Техническое интервью по разработке программного обеспечения. Возможны русские фразы с английскими техническими терминами.
VOICE_TTS_LANGUAGE=ru
VOICE_TTS_MODEL_ID=v4_ru
VOICE_TTS_SPEAKER=baya
VOICE_TTS_SAMPLE_RATE=48000
```

For NVIDIA GPU/CUDA STT:

```env
VOICE_STT_MODEL_SIZE=large-v3-turbo
VOICE_STT_DEVICE=cuda
VOICE_STT_COMPUTE_TYPE=int8_float16
```

For maximum quality, if VRAM allows, try:

```env
VOICE_STT_MODEL_SIZE=large-v3
VOICE_STT_COMPUTE_TYPE=int8_float16
```

For lower latency or VRAM pressure, use `medium` instead of `large-v3-turbo`.
