from faster_whisper import WhisperModel

from app.core.config import Settings


class SttService:
    def __init__(self, settings: Settings) -> None:
        self._settings = settings
        self._model: WhisperModel | None = None

    @property
    def model(self) -> WhisperModel:
        if self._model is None:
            self._model = WhisperModel(
                self._settings.stt_model_size,
                device=self._settings.stt_device,
                compute_type=self._settings.stt_compute_type,
            )
        return self._model

    def transcribe(self, audio_path: str, initial_prompt: str | None = None) -> tuple[str, str | None]:
        segments, info = self.model.transcribe(
            audio_path,
            language=self._settings.stt_language,
            initial_prompt=initial_prompt or self._settings.stt_initial_prompt,
            vad_filter=True,
        )
        text = " ".join(segment.text.strip() for segment in segments).strip()
        return text, info.language
