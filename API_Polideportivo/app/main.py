from fastapi import FastAPI
from app.db.session import setup_database
from app.routers import pistas, reservas

app = FastAPI()

@app.on_event("startup")
async def startup():
    setup_database()

app.include_router(pistas.router)
app.include_router(reservas.router)
