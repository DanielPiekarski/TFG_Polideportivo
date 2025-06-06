﻿using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using WebPolideportivo.Models;

namespace WebPolideportivo.Services;

public class ApiService
{
    private readonly HttpClient _http;
    private readonly AuthService _auth;

    public ApiService(HttpClient http, AuthService auth)
    {
        _http = http;
        _auth = auth;
    }
    public async Task<List<Pista>> GetPistasAsync()
    {
        return await _http.GetFromJsonAsync<List<Pista>>("http://localhost:8000/api/pistas/");
    }

    public async Task<List<DateTime>> GetDisponibilidadAsync(int pistaId, DateTime fecha)
    {
        var url = $"http://localhost:8000/api/reservas/{pistaId}/disponibilidad?fecha={fecha:yyyy-MM-dd}";
        var response = await _http.GetFromJsonAsync<Disponibilidad>(url);
        return response.Disponibles;
    }

    public async Task<Pista?> GetPistaPorIdAsync(int id)
    {
        var url = $"http://localhost:8000/api/pistas/{id}";
        return await _http.GetFromJsonAsync<Pista>(url);
    }

    public async Task ActualizarDisponibilidadAsync(int pistaId, DateTime fecha, List<DateTime> nuevaDisponibilidad)
    {
        var url = $"http://localhost:8000/api/reservas/{pistaId}/disponibilidad?fecha={fecha:yyyy-MM-dd}";
        var payload = new
        {
            disponibles = nuevaDisponibilidad.Select(d => d.ToString("yyyy-MM-ddTHH:mm:ss"))
        };

        var content = new StringContent(JsonSerializer.Serialize(payload), Encoding.UTF8, "application/json");
        var response = await _http.PostAsync(url, content);

        response.EnsureSuccessStatusCode();
    }

    public async Task CrearReservaAsync(int pistaId, DateTime inicio, DateTime fin)
    {
        if (string.IsNullOrEmpty(_auth.Token?.Access_Token))
            throw new InvalidOperationException("No autenticado.");

        var reserva = new
        {
            pista_id = pistaId,
            fecha_hora_inicio = inicio.ToString("o"),
            fecha_hora_fin = fin.ToString("o")
        };

        using var request = new HttpRequestMessage(HttpMethod.Post, "http://localhost:8000/api/reservas/")
        {
            Content = JsonContent.Create(reserva)
        };
        request.Headers.Authorization =
            new AuthenticationHeaderValue("Bearer", _auth.Token.Access_Token);

        var response = await _http.SendAsync(request);
        response.EnsureSuccessStatusCode();
    }

    public async Task<List<Reserva>> GetMisReservasAsync()
    {
        var request = new HttpRequestMessage(HttpMethod.Get, "http://localhost:8000/api/reservas/mis-reservas");
        if (!string.IsNullOrEmpty(_auth.Token?.Access_Token))
        {
            request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", _auth.Token.Access_Token);
        }
        else
        {
            throw new InvalidOperationException("No hay token de autenticación disponible.");
        }

        var response = await _http.SendAsync(request);
        if (!response.IsSuccessStatusCode)
            return new List<Reserva>();

        var content = await response.Content.ReadAsStringAsync();
        return JsonSerializer.Deserialize<List<Reserva>>(content, new JsonSerializerOptions { PropertyNameCaseInsensitive = true }) ?? new List<Reserva>();
    }

    public async Task EliminarReservaAsync(int reservaId)
    {
        if (string.IsNullOrEmpty(_auth.Token?.Access_Token))
            throw new InvalidOperationException("No autenticado.");

        using var request = new HttpRequestMessage(HttpMethod.Delete, $"http://localhost:8000/api/reservas/{reservaId}");
        request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", _auth.Token.Access_Token);

        var response = await _http.SendAsync(request);
        response.EnsureSuccessStatusCode();
    }

    public async Task<TokenDto?> Login(UserDto usuario)
    {
        var dict = new Dictionary<string, string>
    {
        { "username", usuario.Nombre },
        { "password", usuario.Contraseña }
    };

        var content = new FormUrlEncodedContent(dict);
        var response = await _http.PostAsync("http://localhost:8000/auth/login", content);

        if (response.IsSuccessStatusCode)
        {
            var token = await response.Content.ReadFromJsonAsync<TokenDto>();
            if (token != null)
            {
                _auth.IniciarSesion(token, usuario);
            }
            return token;
        }

        return null;
    }

    public async Task<bool> Register(UserDto usuario)
    {
        var response = await _http.PostAsJsonAsync("http://localhost:8000/auth/register", usuario);
        return response.IsSuccessStatusCode;
    }
}

