package com.example.appadminpolideportivo.Models

import com.google.gson.annotations.SerializedName
import java.util.*

data class Reserva(
    @SerializedName("pista_id")
    val pistaId: Int,

    @SerializedName("fecha_hora_inicio")
    val fechaHoraInicio: String,

    @SerializedName("fecha_hora_fin")
    val fechaHoraFin: String
)
