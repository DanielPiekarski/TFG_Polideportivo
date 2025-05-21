from fastapi import APIRouter, Depends, HTTPException
from sqlmodel import Session, select
from app.db.session import get_session
from app.models.reserva import Reserva
from app.models.pista import Pista
from app.schemas.reserva import ReservaCreate, ReservaOut
from app.core.auth import get_current_user
from app.models.usuario import Usuario
from app.services.reserva_service import obtener_disponibilidad, calcular_precio
from datetime import datetime

router = APIRouter(prefix="/api/reservas", tags=["Reservas"])

@router.get("/mis-reservas", response_model=list[ReservaOut])
def mis_reservas(
    usuario: Usuario = Depends(get_current_user),
    session: Session = Depends(get_session)
):
    reservas = session.exec(
        select(Reserva).where(Reserva.usuario_id == usuario.id)
    ).all()

    reservas_out = []
    for reserva in reservas:
        pista = session.exec(select(Pista).where(Pista.id == reserva.pista_id)).first()

        precio_total = calcular_precio(reserva.fecha_hora_inicio, reserva.fecha_hora_fin, pista.precio_por_hora)

        reservas_out.append({
            "id": reserva.id,
            "pista_id": reserva.pista_id,
            "usuario_id": reserva.usuario_id,
            "fecha_hora_inicio": reserva.fecha_hora_inicio,
            "fecha_hora_fin": reserva.fecha_hora_fin,
            "nombre_pista": pista.nombre,
            "precio_total": precio_total
        })

    return reservas_out

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
def reservar(
    reserva: ReservaCreate,
    session: Session = Depends(get_session),
    usuario: Usuario = Depends(get_current_user)
):
    existe_reserva = session.exec(
        select(Reserva).where(
            Reserva.pista_id == reserva.pista_id,
            Reserva.fecha_hora_inicio < reserva.fecha_hora_fin,
            Reserva.fecha_hora_fin > reserva.fecha_hora_inicio
        )
    ).first()

    if existe_reserva:
        raise HTTPException(status_code=400, detail="La pista ya está reservada en ese horario")

    nueva_reserva = Reserva(
        pista_id=reserva.pista_id,
        fecha_hora_inicio=reserva.fecha_hora_inicio,
        fecha_hora_fin=reserva.fecha_hora_fin,
        usuario_id=usuario.id
    )

    session.add(nueva_reserva)
    session.commit()
    session.refresh(nueva_reserva)
    return nueva_reserva


@router.delete("/{id}")
def cancelar_reserva(
    id: int,
    session: Session = Depends(get_session),
    usuario: Usuario = Depends(get_current_user)
):
    reserva = session.get(Reserva, id)
    
    if not reserva:
        raise HTTPException(status_code=404, detail="Reserva no encontrada")
    
    if reserva.usuario_id != usuario.id:
        raise HTTPException(status_code=403, detail="No autorizado para eliminar esta reserva")

    session.delete(reserva)
    session.commit()
    return {"detail": "Reserva cancelada"}