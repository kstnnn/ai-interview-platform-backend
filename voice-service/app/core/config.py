from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_name: str = "voice-service"
    stt_model_size: str = "medium"
    stt_device: str = "cpu"
    stt_compute_type: str = "int8"
    stt_language: str = "ru"
    stt_initial_prompt: str = (
        "Техническое интервью по разработке программного обеспечения. "
        "Возможны русские фразы с английскими техническими терминами, названиями технологий, "
        "классов, фреймворков, протоколов, баз данных и аббревиатурами."
    )
    tts_language: str = "ru"
    tts_model_id: str = "v4_ru"
    tts_speaker: str = "baya"
    tts_sample_rate: int = 48_000

    model_config = SettingsConfigDict(
        env_file=(".env.local", ".env"),
        env_prefix="VOICE_",
        extra="ignore",
    )


@lru_cache
def get_settings() -> Settings:
    return Settings()
