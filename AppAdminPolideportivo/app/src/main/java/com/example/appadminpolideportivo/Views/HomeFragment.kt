package com.example.appadminpolideportivo.Views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.appadminpolideportivo.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val agregarPistaBtn = view.findViewById<Button>(R.id.buttonAgregarPista)
        val actualizarPistaBtn = view.findViewById<Button>(R.id.buttonActualizarPista)
        val eliminarPistaBtn = view.findViewById<Button>(R.id.buttonEliminarPista)
        val quitarHorarioBtn = view.findViewById<Button>(R.id.buttonQuitarDisponibilidad)

        agregarPistaBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_agregarPistaFragment)
        }

        actualizarPistaBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_actualizarPistaFragment)
        }

        eliminarPistaBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_eliminarPistaFragment)
        }

        quitarHorarioBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_eliminarHorariosFragment)
        }

        return view
    }

}