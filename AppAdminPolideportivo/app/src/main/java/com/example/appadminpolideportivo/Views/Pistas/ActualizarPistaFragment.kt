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
import com.example.appadminpolideportivo.Models.PistaUpdate
import com.example.appadminpolideportivo.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActualizarPistaFragment : Fragment() {

    private lateinit var spinner: Spinner
    private lateinit var nombreInput: EditText
    private lateinit var tipoInput: EditText
    private lateinit var precioInput: EditText
    private lateinit var btnActualizar: Button

    private var pistas: List<Pista> = emptyList()
    private var token: String? = null
    private var pistaSeleccionada: Pista? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_actualizar_pista, container, false)

        spinner = view.findViewById(R.id.spinnerPistas)
        nombreInput = view.findViewById(R.id.nombrePistaInput)
        tipoInput = view.findViewById(R.id.tipoPistaInput)
        precioInput = view.findViewById(R.id.precioPistaInput)
        btnActualizar = view.findViewById(R.id.btnActualizarPista)

        val sharedPref = requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        token = sharedPref.getString("TOKEN", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return view
        }

        cargarPistas(token!!)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                pistaSeleccionada = pistas.getOrNull(position)
                pistaSeleccionada?.let { pista ->
                    nombreInput.setText(pista.nombre)
                    tipoInput.setText(pista.tipo)
                    precioInput.setText(pista.precio_por_hora.toString())
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                pistaSeleccionada = null
                nombreInput.text?.clear()
                tipoInput.text?.clear()
                precioInput.text?.clear()
            }
        }

        btnActualizar.setOnClickListener {
            val pista = pistaSeleccionada
            if (pista == null) {
                Toast.makeText(requireContext(), "Selecciona una pista para actualizar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nuevoNombre = nombreInput.text.toString().trim()
            val nuevoTipo = tipoInput.text.toString().trim()
            val nuevoPrecioTexto = precioInput.text.toString().trim()

            if (nuevoNombre.isEmpty() || nuevoTipo.isEmpty() || nuevoPrecioTexto.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nombreRepetido = pistas.any {
                it.nombre.equals(nuevoNombre, ignoreCase = true) && it.id != pista.id
            }
            if (nombreRepetido) {
                Toast.makeText(requireContext(), "Ya existe otra pista con ese nombre", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nuevoPrecio = nuevoPrecioTexto.toDoubleOrNull()
            if (nuevoPrecio == null) {
                Toast.makeText(requireContext(), "Precio inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val pistaUpdate = PistaUpdate(
                nombre = nuevoNombre,
                tipo = nuevoTipo,
                precio_por_hora = nuevoPrecio
            )

            RetrofitClient.instance.actualizarPista("Bearer $token", pista.id, pistaUpdate)
                .enqueue(object : Callback<Pista> {
                    override fun onResponse(call: Call<Pista>, response: Response<Pista>) {
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Pista actualizada correctamente", Toast.LENGTH_SHORT).show()
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
                        pistas = response.body() ?: emptyList()
                        val nombres = pistas.map { it.nombre }
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinner.adapter = adapter
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
