from pathlib import Path

from fastapi import APIRouter, File, Form, HTTPException, Response, UploadFile, status

from app.core.config import get_settings
from app.schemas.voice import SynthesisRequest, TranscriptionResponse
from app.services.stt_service import SttService
from app.services.tts_service import TtsService
from app.utils.audio import save_upload_to_temp_file

router = APIRouter(prefix="/api/v1/voice", tags=["voice"])

_stt_service = SttService(get_settings())
_tts_service = TtsService(get_settings())

ALLOWED_AUDIO_EXTENSIONS = {".wav", ".mp3", ".m4a", ".webm", ".ogg", ".flac"}
ALLOWED_FALLBACK_CONTENT_TYPES = {
    "application/octet-stream",
    "binary/octet-stream",
    "video/webm",
}


@router.post("/transcribe", response_model=TranscriptionResponse)
async def transcribe(
    audio: UploadFile = File(...), initial_prompt: str | None = Form(default=None)
) -> TranscriptionResponse:
    if not is_supported_audio_upload(audio):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Uploaded file must be a supported audio file",
        )

    temp_path = await save_upload_to_temp_file(audio)
    try:
        text, language = _stt_service.transcribe(temp_path, initial_prompt)
        return TranscriptionResponse(text=text, language=language)
    finally:
        Path(temp_path).unlink(missing_ok=True)


@router.post("/synthesize", response_class=Response)
async def synthesize(request: SynthesisRequest) -> Response:
    audio = _tts_service.synthesize(request.text, request.speaker)
    return Response(content=audio, media_type="audio/wav")


def is_supported_audio_upload(audio: UploadFile) -> bool:
    content_type = (audio.content_type or "").lower()
    extension = Path(audio.filename or "").suffix.lower()

    if content_type.startswith("audio/"):
        return True
    if content_type in ALLOWED_FALLBACK_CONTENT_TYPES and extension in ALLOWED_AUDIO_EXTENSIONS:
        return True
    if not content_type and extension in ALLOWED_AUDIO_EXTENSIONS:
        return True
    return False
