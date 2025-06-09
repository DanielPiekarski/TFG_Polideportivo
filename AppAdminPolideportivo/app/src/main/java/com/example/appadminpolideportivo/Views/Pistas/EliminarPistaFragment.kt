package com.example.appadminpolideportivo.Views.Pistas

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.appadminpolideportivo.Api.RetrofitClient
import com.example.appadminpolideportivo.Models.Pista
import com.example.appadminpolideportivo.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EliminarPistaFragment : Fragment() {

    private lateinit var spinner: Spinner
    private lateinit var btnEliminar: Button
    private var pistas: List<Pista> = emptyList()
    private var token: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_eliminar_pista, container, false)

        spinner = view.findViewById(R.id.spinnerPistas)
        btnEliminar = view.findViewById(R.id.btnEliminarPista)

        val sharedPref = requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        token = sharedPref.getString("TOKEN", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return view
        }

        cargarPistas(token!!)

        btnEliminar.setOnClickListener {
            val index = spinner.selectedItemPosition
            if (index < 0 || index >= pistas.size) {
                Toast.makeText(requireContext(), "Selecciona una pista", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val pistaSeleccionada = pistas[index]

            AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Pista")
                .setMessage("¿Estás seguro de que quieres eliminar la pista '${pistaSeleccionada.nombre}'?")
                .setPositiveButton("Sí") { _, _ ->
                    RetrofitClient.instance.eliminarPista("Bearer $token", pistaSeleccionada.id)
                        .enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Toast.makeText(requireContext(), "Pista eliminada", Toast.LENGTH_SHORT).show()
                                    cargarPistas(token!!)
                                } else {
                                    Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                }
                .setNegativeButton("Cancelar", null)
                .show()
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
