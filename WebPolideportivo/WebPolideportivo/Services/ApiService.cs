using Microsoft.AspNetCore.Components.Forms;
using System.Net.Http;
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
        return await _http.GetFromJsonAsync<List<Pista>>("https://api-polideportivo.onrender.com/api/pistas/");
    }

    public async Task<List<DateTime>> GetDisponibilidadAsync(int pistaId, DateTime fecha)
    {
        var url = $"https://api-polideportivo.onrender.com/api/reservas/{pistaId}/disponibilidad?fecha={fecha:yyyy-MM-dd}";
        var response = await _http.GetFromJsonAsync<Disponibilidad>(url);
        return response.Disponibles;
    }

    public async Task<Pista?> GetPistaPorIdAsync(int id)
    {
        var url = $"https://api-polideportivo.onrender.com/api/pistas/{id}";
        return await _http.GetFromJsonAsync<Pista>(url);
    }

    public async Task ActualizarDisponibilidadAsync(int pistaId, DateTime fecha, List<DateTime> nuevaDisponibilidad)
    {
        var url = $"https://api-polideportivo.onrender.com/api/reservas/{pistaId}/disponibilidad?fecha={fecha:yyyy-MM-dd}";
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

        using var request = new HttpRequestMessage(HttpMethod.Post, "https://api-polideportivo.onrender.com/api/reservas/")
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
        var request = new HttpRequestMessage(HttpMethod.Get, "https://api-polideportivo.onrender.com/api/reservas/mis-reservas");
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

        using var request = new HttpRequestMessage(HttpMethod.Delete, $"https://api-polideportivo.onrender.com/api/reservas/{reservaId}");
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
        var response = await _http.PostAsync("https://api-polideportivo.onrender.com/auth/login", content);

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
        var response = await _http.PostAsJsonAsync("https://api-polideportivo.onrender.com/auth/register", usuario);
        return response.IsSuccessStatusCode;
    }

    public async Task<bool> SubirFotoPerfilAsync(IBrowserFile file)
    {
        if (string.IsNullOrEmpty(_auth.Token?.Access_Token))
            throw new InvalidOperationException("No autenticado.");

        var content = new MultipartFormDataContent();
        var stream = file.OpenReadStream(maxAllowedSize: 5 * 1024 * 1024);
        var fileContent = new StreamContent(stream);
        fileContent.Headers.ContentType = new MediaTypeHeaderValue(file.ContentType);

        content.Add(fileContent, "file", file.Name);

        var request = new HttpRequestMessage(HttpMethod.Put, "https://api-polideportivo.onrender.com/auth/usuario/foto");
        request.Content = content;
        request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", _auth.Token.Access_Token);

        var response = await _http.SendAsync(request);
        return response.IsSuccessStatusCode;
    }

    public async Task<UserDto> ObtenerMiPerfilAsync()
    {
        var request = new HttpRequestMessage(HttpMethod.Get, "https://api-polideportivo.onrender.com/auth/usuario/me");
        request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", _auth.Token.Access_Token);

        var response = await _http.SendAsync(request);
        response.EnsureSuccessStatusCode();

        var json = await response.Content.ReadAsStringAsync();
        return JsonSerializer.Deserialize<UserDto>(json, new JsonSerializerOptions { PropertyNameCaseInsensitive = true })!;
    }
}

