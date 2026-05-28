from fastapi import FastAPI

from app.api.routes import health, voice
from app.core.config import get_settings


def create_app() -> FastAPI:
    settings = get_settings()
    app = FastAPI(title=settings.app_name)
    app.include_router(health.router)
    app.include_router(voice.router)
    return app


app = create_app()
