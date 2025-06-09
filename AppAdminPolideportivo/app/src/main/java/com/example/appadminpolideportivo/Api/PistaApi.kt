package com.example.appadminpolideportivo.Api

import com.example.appadminpolideportivo.Models.Pista
import com.example.appadminpolideportivo.Models.PistaUpdate
import retrofit2.Call
import retrofit2.http.*

interface PistaApi {

    @GET("/api/pistas/")
    fun obtenerPistas(
        @Header("Authorization") token: String
    ): Call<List<Pista>>

    @POST("/api/pistas/")
    fun crearPista(
        @Header("Authorization") token: String,
        @Body pista: Pista
    ): Call<Pista>

    @DELETE("/api/pistas/{id}")
    fun eliminarPista(
        @Header("Authorization") token: String,
        @Path("id") pistaId: Int
    ): Call<Void>

    @PUT("/api/pistas/{id}")
    fun actualizarPista(
        @Header("Authorization") token: String,
        @Path("id") pistaId: Int,
        @Body pistaUpdate: PistaUpdate
    ): Call<Pista>
}
