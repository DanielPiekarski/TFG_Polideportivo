from fastapi import FastAPI
from app.db.session import setup_database
from app.routers import pistas, reservas, auth
from fastapi.staticfiles import StaticFiles

app = FastAPI()

@app.on_event("startup")
async def startup():
    setup_database()

app.include_router(pistas.router)
app.include_router(reservas.router)
app.include_router(auth.router)

app.mount("/uploads", StaticFiles(directory="uploads"), name="uploads")