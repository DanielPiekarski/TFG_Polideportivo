from sqlmodel import SQLModel, Field
from typing import Optional

class Usuario(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    nombre: str = Field(index=True, unique=True)
    hashed_password: str
    rol: str = Field(default="usuario")
    foto_perfil: Optional[str] = None