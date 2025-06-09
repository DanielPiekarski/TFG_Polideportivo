from fastapi import APIRouter, Depends, HTTPException, status, UploadFile, File
from sqlmodel import Session, select
from fastapi.security import OAuth2PasswordRequestForm
from app.db.session import get_session
from app.models.usuario import Usuario
from app.schemas.usuario import UsuarioCreate, UsuarioOut, Token
from app.core import auth
from datetime import datetime
import shutil
import os

router = APIRouter(prefix="/auth", tags=["Auth"])

UPLOAD_DIR = "uploads/fotos_perfil"
os.makedirs(UPLOAD_DIR, exist_ok=True)

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

@router.put("/usuario/foto")
async def actualizar_foto_perfil(
    file: UploadFile = File(...),
    current_user: Usuario = Depends(auth.get_current_user),
    session: Session = Depends(get_session),
):
    if file.content_type not in ["image/jpeg", "image/png"]:
        raise HTTPException(status_code=400, detail="Formato no soportado. Usa JPEG o PNG")

    extension = os.path.splitext(file.filename)[1]
    nombre_archivo = f"user_{current_user.id}_{int(datetime.utcnow().timestamp())}{extension}"
    ruta_guardar = os.path.join(UPLOAD_DIR, nombre_archivo)

    with open(ruta_guardar, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    current_user.foto_perfil = f"/uploads/fotos_perfil/{nombre_archivo}"
    session.add(current_user)
    session.commit()
    session.refresh(current_user)

    return {"mensaje": "Foto actualizada correctamente", "foto_perfil": current_user.foto_perfil}

@router.get("/usuario/me", response_model=UsuarioOut)
def leer_usuario_actual(current_user: Usuario = Depends(auth.get_current_user)):
    return current_user
