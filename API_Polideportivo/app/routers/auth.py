from fastapi import APIRouter, Depends, HTTPException, status
from sqlmodel import Session, select
from fastapi.security import OAuth2PasswordRequestForm
from app.db.session import get_session
from app.models.usuario import Usuario
from app.schemas.usuario import UsuarioCreate, UsuarioOut, Token
from app.core import auth

router = APIRouter(prefix="/auth", tags=["Auth"])

@router.post("/register", response_model=UsuarioOut)
def register(usuario: UsuarioCreate, session: Session = Depends(get_session)):
    usuario_exist = session.exec(select(Usuario).where(Usuario.nombre == usuario.nombre)).first()
    if usuario_exist:
        raise HTTPException(status_code=400, detail="Nombre ya registrado")
    hashed_password = auth.get_password_hash(usuario.contraseña)
    new_usuario = Usuario(nombre=usuario.nombre, hashed_password=hashed_password, rol="usuario")
    session.add(new_usuario)
    session.commit()
    session.refresh(new_usuario)
    return new_usuario

@router.post("/login", response_model=Token)
def login(form_data: OAuth2PasswordRequestForm = Depends(), session: Session = Depends(get_session)):
    usuario = session.exec(select(Usuario).where(Usuario.nombre == form_data.username)).first()
    if not usuario or not auth.verify_password(form_data.password, usuario.hashed_password):
        raise HTTPException(status_code=400, detail="Credenciales inválidas")
    access_token = auth.create_access_token(data={"sub": usuario.nombre, "rol": usuario.rol})
    return {"access_token": access_token, "token_type": "bearer"}
