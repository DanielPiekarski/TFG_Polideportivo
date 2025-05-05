using WebPolideportivo.Models;
using WebPolideportivo.Pages;

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
}

