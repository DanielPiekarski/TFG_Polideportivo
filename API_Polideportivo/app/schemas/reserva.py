from pydantic import BaseModel
from datetime import datetime

class ReservaCreate(BaseModel):
    pista_id: int
    fecha_hora_inicio: datetime
    fecha_hora_fin: datetime
