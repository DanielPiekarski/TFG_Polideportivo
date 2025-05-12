from pydantic_settings import BaseSettings

class Config(BaseSettings):
    database_url: str = "sqlite:///./data/database.db"
    debug: bool = False

    class Config:
        env_file = ".env"

config = Config()
