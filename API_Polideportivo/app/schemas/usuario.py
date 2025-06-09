from pydantic import BaseModel
from typing import Optional

class UsuarioCreate(BaseModel):
    nombre: str
    contraseña: str

class UsuarioOut(BaseModel):
    id: int
    nombre: str
    rol: str
    foto_perfil: Optional[str] = None

    class Config:
        orm_mode = True

class Token(BaseModel):
    access_token: str
    token_type: str
