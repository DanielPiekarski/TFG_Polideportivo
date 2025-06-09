package com.example.appadminpolideportivo.Views

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.appadminpolideportivo.Api.RetrofitClient
import com.example.appadminpolideportivo.R
import com.example.appadminpolideportivo.Utils.TokenResponse
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Base64
import org.json.JSONObject

class LoginFragment : Fragment() {

    private fun base64Decode(input: String): ByteArray {
        return Base64.decode(input, Base64.URL_SAFE or Base64.NO_WRAP)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val nombreUsuario = view.findViewById<TextInputEditText>(R.id.nombreUsuario)
        val contrasenaUsuario = view.findViewById<TextInputEditText>(R.id.contraseñaUsuario)
        val botonEntrar = view.findViewById<Button>(R.id.button)

        botonEntrar.setOnClickListener {
            val username = nombreUsuario.text.toString().trim()
            val password = contrasenaUsuario.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            botonEntrar.isEnabled = false

            RetrofitClient.authService.login(username, password)
                .enqueue(object : Callback<TokenResponse> {
                    override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                        botonEntrar.isEnabled = true

                        if (response.isSuccessful) {
                            val token = response.body()?.access_token ?: ""
                            val parts = token.split(".")
                            if (parts.size == 3) {
                                try {
                                    val decodedBytes = base64Decode(parts[1])
                                    val payload = String(decodedBytes)
                                    val json = JSONObject(payload)
                                    val rol = json.optString("rol", "")

                                    if (rol == "admin") {
                                        val sharedPref = requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
                                        with(sharedPref.edit()) {
                                            putString("TOKEN", token)
                                            apply()
                                        }

                                        Toast.makeText(requireContext(), "Login correcto", Toast.LENGTH_SHORT).show()
                                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                                    } else {
                                        Toast.makeText(requireContext(), "Acceso denegado. Solo administradores.", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(requireContext(), "Error al decodificar token", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(requireContext(), "Token inválido", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(requireContext(), "Nombre o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                        botonEntrar.isEnabled = true
                        Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        return view
    }
}
