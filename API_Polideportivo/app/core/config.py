from pydantic_settings import BaseSettings

class Config(BaseSettings):
    database_url: str
    secret_key: str
    algorithm: str = "HS256"
    access_token_expire_minutes: int = 30
    debug: bool = False

    class Config:
        env_file = ".env"

config = Config()