from sqlmodel import Session, select
from app.models.reserva import Reserva
from datetime import datetime, timedelta

def obtener_disponibilidad(session: Session, pista_id: int, fecha: datetime):
    reservas = session.exec(select(Reserva).where(Reserva.pista_id == pista_id)).all()
    horarios_disponibles = []
    hora_apertura, hora_cierre = 8, 22

    for hora in range(hora_apertura, hora_cierre):
        fecha_hora = fecha.replace(hour=hora, minute=0, second=0, microsecond=0)
        if not any(r.fecha_hora_inicio < fecha_hora + timedelta(hours=1) and r.fecha_hora_fin > fecha_hora for r in reservas):
            horarios_disponibles.append(fecha_hora)

    return horarios_disponibles

def calcular_precio(inicio: datetime, fin: datetime, precio_por_hora: float) -> float:
    duracion_horas = (fin - inicio).total_seconds() / 3600
    return round(precio_por_hora * duracion_horas, 2)
