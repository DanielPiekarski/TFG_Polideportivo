from pydantic import BaseModel
from datetime import datetime

class ReservaCreate(BaseModel):
    pista_id: int
    fecha_hora_inicio: datetime
    fecha_hora_fin: datetime

class ReservaOut(BaseModel):
    id: int
    pista_id: int
    usuario_id: int
    fecha_hora_inicio: datetime
    fecha_hora_fin: datetime
    nombre_pista: str
    precio_total: float

    class Config:
        orm_mode = True
