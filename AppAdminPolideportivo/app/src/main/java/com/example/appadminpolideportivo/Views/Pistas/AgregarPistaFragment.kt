package com.example.appadminpolideportivo.Views.Pistas

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.appadminpolideportivo.Api.RetrofitClient
import com.example.appadminpolideportivo.Models.Pista
import com.example.appadminpolideportivo.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AgregarPistaFragment : Fragment() {

    private var pistasExistentes: List<Pista> = emptyList()
    private var token: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_agregar_pista, container, false)

        val nombreInput = view.findViewById<EditText>(R.id.nombrePistaInput)
        val tipoInput = view.findViewById<EditText>(R.id.tipoPistaInput)
        val precioInput = view.findViewById<EditText>(R.id.precioPistaInput)
        val boton = view.findViewById<Button>(R.id.btnAgregarPista)

        val sharedPref = requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        token = sharedPref.getString("TOKEN", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return view
        }

        cargarPistas(token!!)

        boton.setOnClickListener {
            val nombre = nombreInput.text.toString().trim()
            val tipo = tipoInput.text.toString().trim()
            val precioTexto = precioInput.text.toString().trim()

            if (nombre.isEmpty() || tipo.isEmpty() || precioTexto.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pistasExistentes.any { it.nombre.equals(nombre, ignoreCase = true) }) {
                Toast.makeText(requireContext(), "Ya existe una pista con ese nombre", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val precio = precioTexto.toDoubleOrNull()
            if (precio == null) {
                Toast.makeText(requireContext(), "Precio inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nuevaPista = Pista(
                id = 0,
                nombre = nombre,
                tipo = tipo,
                precio_por_hora = precio
            )

            RetrofitClient.instance.crearPista("Bearer $token", nuevaPista)
                .enqueue(object : Callback<Pista> {
                    override fun onResponse(call: Call<Pista>, response: Response<Pista>) {
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Pista creada correctamente", Toast.LENGTH_SHORT).show()
                            nombreInput.text?.clear()
                            tipoInput.text?.clear()
                            precioInput.text?.clear()
                            cargarPistas(token!!)
                        } else {
                            Toast.makeText(requireContext(), "Error: ${response.code()} - ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Pista>, t: Throwable) {
                        Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        return view
    }

    private fun cargarPistas(token: String) {
        RetrofitClient.instance.obtenerPistas("Bearer $token")
            .enqueue(object : Callback<List<Pista>> {
                override fun onResponse(call: Call<List<Pista>>, response: Response<List<Pista>>) {
                    if (response.isSuccessful) {
                        pistasExistentes = response.body() ?: emptyList()
                    } else {
                        Toast.makeText(requireContext(), "Error al obtener pistas", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Pista>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
