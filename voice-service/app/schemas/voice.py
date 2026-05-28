from pydantic import BaseModel, Field


class TranscriptionResponse(BaseModel):
    text: str
    language: str | None = None


class SynthesisRequest(BaseModel):
    text: str = Field(..., min_length=1, max_length=4_000)
    speaker: str | None = Field(default=None, max_length=64)
