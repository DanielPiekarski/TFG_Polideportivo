using System.Text.Json.Serialization;

namespace WebPolideportivo.Models;

public class Reserva
{
    public int Id { get; set; }
    public int PistaId { get; set; }
    public int UsuarioId { get; set; }

    [JsonPropertyName("fecha_hora_inicio")]
    public DateTime FechaHoraInicio { get; set; }
    [JsonPropertyName("fecha_hora_fin")]
    public DateTime FechaHoraFin { get; set; }

    [JsonPropertyName("nombre_pista")]
    public string NombrePista { get; set; } = string.Empty;
    [JsonPropertyName("precio_total")]
    public decimal PrecioTotal { get; set; }

    public DateTime Fecha => FechaHoraInicio.Date;
    public string HoraInicio => FechaHoraInicio.ToString("HH:mm");
    public string HoraFin => FechaHoraFin.ToString("HH:mm");
}



