from sqlmodel import SQLModel, Field

class Usuario(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    nombre: str = Field(index=True, unique=True)
    hashed_password: str
    rol: str = Field(default="usuario")
