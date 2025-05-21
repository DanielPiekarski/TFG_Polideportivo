from sqlmodel import SQLModel, Field
from datetime import datetime

class Reserva(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    pista_id: int = Field(foreign_key="pista.id")
    usuario_id: int = Field(foreign_key="usuario.id")
    fecha_hora_inicio: datetime
    fecha_hora_fin: datetime

    class Config:
        arbitrary_types_allowed = True
