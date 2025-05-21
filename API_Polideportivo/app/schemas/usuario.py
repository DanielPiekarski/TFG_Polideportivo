from pydantic import BaseModel

class UsuarioCreate(BaseModel):
    nombre: str
    contraseña: str

class UsuarioOut(BaseModel):
    id: int
    nombre: str
    rol: str

    class Config:
        orm_mode = True

class Token(BaseModel):
    access_token: str
    token_type: str
