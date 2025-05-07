using System.Text.Json.Serialization;

namespace WebPolideportivo.Models;

public class Pista
{
    public int Id { get; set; }
    public string Nombre { get; set; }
    public string Tipo { get; set; }

    [JsonPropertyName("precio_por_hora")]
    public decimal PrecioPorHora { get; set; }
    public string? ImagenNombre { get; set; }

}
