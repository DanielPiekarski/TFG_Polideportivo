package com.example.appadminpolideportivo.Views.Pistas

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.appadminpolideportivo.Api.RetrofitClient
import com.example.appadminpolideportivo.Models.Disponibilidad
import com.example.appadminpolideportivo.Models.Pista
import com.example.appadminpolideportivo.Models.Reserva
import com.example.appadminpolideportivo.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class EliminarHorariosFragment : Fragment() {

    private lateinit var spinnerHoras: Spinner
    private lateinit var buttonEliminarHorario: Button
    private lateinit var editTextFecha: EditText
    private lateinit var imageViewCalendario: ImageView
    private lateinit var spinnerPistas: Spinner

    private var fechaSeleccionada: Date? = null
    private var horasDisponibles: List<String> = emptyList()
    private var pistaIdSeleccionada: Int = -1
    private var pistas: List<Pista> = emptyList()
    private var token: String? = null

    private val dateFormatAPI = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateFormatUI = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_eliminar_horarios, container, false)

        spinnerHoras = view.findViewById(R.id.spinnerHoras)
        buttonEliminarHorario = view.findViewById(R.id.buttonEliminarHorario)
        editTextFecha = view.findViewById(R.id.editTextFecha)
        imageViewCalendario = view.findViewById(R.id.imageViewCalendario)
        spinnerPistas = view.findViewById(R.id.spinnerPistas)

        val sharedPref = requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        token = sharedPref.getString("TOKEN", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return view
        }

        // Inicializar fechaSeleccionada al día actual y mostrarla en EditText
        fechaSeleccionada = Calendar.getInstance().time
        editTextFecha.setText(dateFormatUI.format(fechaSeleccionada!!))

        // Abrir DatePicker al pulsar en EditText o en el icono calendario
        editTextFecha.setOnClickListener {
            mostrarSelectorFecha()
        }
        imageViewCalendario.setOnClickListener {
            mostrarSelectorFecha()
        }

        cargarPistas(token!!)

        buttonEliminarHorario.setOnClickListener {
            eliminarHorario()
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
                        spinnerPistas.adapter = adapter

                        spinnerPistas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                                pistaIdSeleccionada = pistas[position].id
                                if (fechaSeleccionada != null) {
                                    cargarDisponibilidad()
                                }
                            }
                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }

                        if (pistas.isNotEmpty()) {
                            spinnerPistas.setSelection(0)
                            pistaIdSeleccionada = pistas[0].id
                            cargarDisponibilidad()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Error al obtener pistas", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Pista>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun mostrarSelectorFecha() {
        val c = Calendar.getInstance()
        if (fechaSeleccionada != null) {
            c.time = fechaSeleccionada!!
        }

        val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            c.set(year, month, dayOfMonth)
            fechaSeleccionada = c.time
            editTextFecha.setText(dateFormatUI.format(fechaSeleccionada!!))
            cargarDisponibilidad()
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))

        val hoy = Calendar.getInstance()
        val dentroDeUnAno = Calendar.getInstance().apply { add(Calendar.YEAR, 1) }

        datePickerDialog.datePicker.minDate = hoy.timeInMillis
        datePickerDialog.datePicker.maxDate = dentroDeUnAno.timeInMillis

        datePickerDialog.show()
    }

    private fun cargarDisponibilidad() {
        if (fechaSeleccionada == null || pistaIdSeleccionada == -1) return

        RetrofitClient.reservaApi.obtenerDisponibilidad("Bearer $token", pistaIdSeleccionada, dateFormatAPI.format(fechaSeleccionada!!))
            .enqueue(object : Callback<Disponibilidad> {
                override fun onResponse(call: Call<Disponibilidad>, response: Response<Disponibilidad>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        horasDisponibles = body?.disponibles ?: emptyList()
                        mostrarHorasEnSpinner()
                    } else {
                        Toast.makeText(requireContext(), "Error al cargar disponibilidad: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Disponibilidad>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error en conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun mostrarHorasEnSpinner() {
        if (horasDisponibles.isEmpty()) {
            spinnerHoras.adapter = null
            Toast.makeText(requireContext(), "No hay horas disponibles para esta fecha y pista", Toast.LENGTH_SHORT).show()
            return
        }

        val horasString = horasDisponibles.map {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = parser.parse(it)
            timeFormat.format(date!!)
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, horasString)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerHoras.adapter = adapter
    }

    private fun eliminarHorario() {
        val horaSeleccionadaPos = spinnerHoras.selectedItemPosition
        if (fechaSeleccionada == null || horaSeleccionadaPos == Spinner.INVALID_POSITION) {
            Toast.makeText(requireContext(), "Selecciona una fecha y una hora", Toast.LENGTH_SHORT).show()
            return
        }

        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val horaInicio = parser.parse(horasDisponibles[horaSeleccionadaPos])!!
        val horaFin = Calendar.getInstance().apply {
            time = horaInicio
            add(Calendar.HOUR_OF_DAY, 1)
        }.time

        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val horaInicioStr = isoFormat.format(horaInicio)
        val horaFinStr = isoFormat.format(horaFin)

        val reserva = Reserva(
            pistaId = pistaIdSeleccionada,
            fechaHoraInicio = horaInicioStr,
            fechaHoraFin = horaFinStr
        )

        val authToken = "Bearer $token"

        RetrofitClient.reservaApi.reservar(authToken, reserva)
            .enqueue(object : Callback<Reserva> {
                override fun onResponse(call: Call<Reserva>, response: Response<Reserva>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Horario eliminado correctamente", Toast.LENGTH_SHORT).show()
                        cargarDisponibilidad()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Toast.makeText(requireContext(), "Error al eliminar horario: $errorBody", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<Reserva>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error en conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
