using System.Net.Http;
using System.Text;
using System.Text.Json;
using WebPolideportivo.Models;

namespace WebPolideportivo.Services;

public class ApiService
{
    private readonly HttpClient _http;

    public ApiService(HttpClient http)
    {
        _http = http;
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

    public async Task CrearReservaAsync(int pistaId, DateTime horaInicio, DateTime horaFin)
    {
        var reservaBackend = new
        {
            pista_id = pistaId,
            fecha_hora_inicio = horaInicio.ToString("o"),
            fecha_hora_fin = horaFin.ToString("o")
        };

        var content = new StringContent(JsonSerializer.Serialize(reservaBackend), Encoding.UTF8, "application/json");
        var response = await _http.PostAsync("http://localhost:8000/api/reservas", content);

        response.EnsureSuccessStatusCode();
    }
}

