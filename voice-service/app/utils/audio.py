from pathlib import Path
from tempfile import NamedTemporaryFile

from fastapi import UploadFile


async def save_upload_to_temp_file(upload: UploadFile) -> str:
    suffix = Path(upload.filename or "audio.wav").suffix or ".wav"
    with NamedTemporaryFile(delete=False, suffix=suffix) as temp_file:
        while chunk := await upload.read(1024 * 1024):
            temp_file.write(chunk)
        return temp_file.name
