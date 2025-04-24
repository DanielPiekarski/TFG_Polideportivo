from fastapi import APIRouter, Depends, HTTPException
from sqlmodel import Session, select
from app.db.session import get_session
from app.models.reserva import Reserva
from app.schemas.reserva import ReservaCreate
from app.services.reserva_service import obtener_disponibilidad
from datetime import datetime

router = APIRouter(prefix="/api/reservas", tags=["Reservas"])

@router.get("/")
def obtener_reservas(session: Session = Depends(get_session)):
    return session.exec(select(Reserva)).all()

@router.get("/{pista_id}")
def obtener_reservas_por_pista(pista_id: int, session: Session = Depends(get_session)):
    reservas = session.exec(select(Reserva).where(Reserva.pista_id == pista_id)).all()
    if not reservas:
        raise HTTPException(status_code=404, detail="No hay reservas para esta pista.")
    return reservas

@router.get("/{pista_id}/disponibilidad")
def disponibilidad(pista_id: int, fecha: datetime, session: Session = Depends(get_session)):
    return {"disponibles": obtener_disponibilidad(session, pista_id, fecha)}

@router.post("/")
def reservar(reserva: ReservaCreate, session: Session = Depends(get_session)):
    existe_reserva = session.exec(
        select(Reserva).where(
            Reserva.pista_id == reserva.pista_id,
            Reserva.fecha_hora_inicio < reserva.fecha_hora_fin,
            Reserva.fecha_hora_fin > reserva.fecha_hora_inicio
        )
    ).first()

    if existe_reserva:
        raise HTTPException(status_code=400, detail="La pista ya está reservada en ese horario")

    nueva_reserva = Reserva(**reserva.dict())
    session.add(nueva_reserva)
    session.commit()
    session.refresh(nueva_reserva)
    return nueva_reserva

@router.delete("/{id}")
def cancelar_reserva(id: int, session: Session = Depends(get_session)):
    reserva = session.get(Reserva, id)
    if not reserva:
        raise HTTPException(status_code=404, detail="Reserva no encontrada")

    session.delete(reserva)
    session.commit()
    return {"detail": "Reserva cancelada"}
