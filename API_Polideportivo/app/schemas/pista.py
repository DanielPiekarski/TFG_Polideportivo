from pydantic import BaseModel

class PistaCreate(BaseModel):
    nombre: str
    tipo: str
    precio_por_hora: float
