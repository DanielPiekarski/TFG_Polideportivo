from fastapi import APIRouter, Depends
from sqlmodel import Session, select
from app.db.session import get_session
from app.models.pista import Pista
from app.schemas.pista import PistaCreate, PistaUpdate
from app.core.auth import get_current_user, admin_required

router = APIRouter(prefix="/api/pistas", tags=["Pistas"])

@router.get("/")
def obtener_pistas(session: Session = Depends(get_session)):
    return session.exec(select(Pista)).all()

@router.get("/{id}")
def obtener_pista_por_id(id: int, session: Session = Depends(get_session)):
    pista = session.get(Pista, id)
    if not pista:
        raise HTTPException(status_code=404, detail="Pista no encontrada")
    return pista

@router.post("/", dependencies=[Depends(admin_required)])
def crear_pista(pista: PistaCreate, session: Session = Depends(get_session)):
    nueva_pista = Pista(**pista.dict())
    session.add(nueva_pista)
    session.commit()
    session.refresh(nueva_pista)
    return nueva_pista

@router.delete("/{id}", dependencies=[Depends(admin_required)])
def eliminar_pista(id: int, session: Session = Depends(get_session)):
    pista = session.get(Pista, id)
    if not pista:
        raise HTTPException(status_code=404, detail="Pista no encontrada")
    
    session.delete(pista)
    session.commit()
    return {"mensaje": "Pista eliminada correctamente"}


@router.put("/{id}", dependencies=[Depends(admin_required)])
def actualizar_pista(id: int, pista_update: PistaUpdate, session: Session = Depends(get_session)):
    pista = session.get(Pista, id)
    if not pista:
        raise HTTPException(status_code=404, detail="Pista no encontrada")

    pista_data = pista_update.dict(exclude_unset=True)
    for key, value in pista_data.items():
        setattr(pista, key, value)

    session.add(pista)
    session.commit()
    session.refresh(pista)
    return pista
