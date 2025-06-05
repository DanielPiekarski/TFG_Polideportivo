from pydantic import BaseModel
from typing import Optional

class PistaCreate(BaseModel):
    nombre: str
    tipo: str
    precio_por_hora: float

class PistaUpdate(BaseModel):
    nombre: Optional[str] = None
    tipo: Optional[str] = None
    precio_por_hora: Optional[float] = None
