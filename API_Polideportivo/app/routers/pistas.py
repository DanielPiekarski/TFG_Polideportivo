from fastapi import APIRouter, Depends
from sqlmodel import Session, select
from app.db.session import get_session
from app.models.pista import Pista
from app.schemas.pista import PistaCreate

router = APIRouter(prefix="/api/pistas", tags=["Pistas"])

@router.get("/")
def obtener_pistas(session: Session = Depends(get_session)):
    return session.exec(select(Pista)).all()

@router.post("/")
def crear_pista(pista: PistaCreate, session: Session = Depends(get_session)):
    nueva_pista = Pista(**pista.dict())
    session.add(nueva_pista)
    session.commit()
    session.refresh(nueva_pista)
    return nueva_pista
