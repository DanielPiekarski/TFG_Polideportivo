package com.example.appadminpolideportivo.Api

import com.example.appadminpolideportivo.Models.Reserva
import com.example.appadminpolideportivo.Models.Disponibilidad
import retrofit2.Call
import retrofit2.http.*

interface ReservaApi {

    @GET("/api/reservas/{pista_id}/disponibilidad")
    fun obtenerDisponibilidad(
        @Header("Authorization") token: String,
        @Path("pista_id") pistaId: Int,
        @Query("fecha") fecha: String
    ): Call<Disponibilidad>

    @POST("/api/reservas/")
    fun reservar(
        @Header("Authorization") token: String,
        @Body reservaCreate: Reserva
    ): Call<Reserva>
}
