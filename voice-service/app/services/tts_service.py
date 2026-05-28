from io import BytesIO

import soundfile as sf
import torch

from app.core.config import Settings
from app.services.tts_normalizer import normalize_text_for_tts


class TtsService:
    def __init__(self, settings: Settings) -> None:
        self._settings = settings
        self._model = None

    @property
    def model(self):
        if self._model is None:
            model, _ = torch.hub.load(
                repo_or_dir="snakers4/silero-models",
                model="silero_tts",
                language=self._settings.tts_language,
                speaker=self._settings.tts_model_id,
                trust_repo=True,
            )
            model.to("cpu")
            self._model = model
        return self._model

    def synthesize(self, text: str, speaker: str | None = None) -> bytes:
        normalized_text = normalize_text_for_tts(text.strip())
        audio = self.model.apply_tts(
            text=normalized_text,
            speaker=speaker or self._settings.tts_speaker,
            sample_rate=self._settings.tts_sample_rate,
        )

        buffer = BytesIO()
        sf.write(buffer, audio.detach().cpu().numpy(), self._settings.tts_sample_rate, format="WAV")
        return buffer.getvalue()
