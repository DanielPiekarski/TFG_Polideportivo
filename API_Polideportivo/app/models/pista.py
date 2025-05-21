from sqlmodel import SQLModel, Field

class Pista(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    nombre: str
    tipo: str
    precio_por_hora: float
