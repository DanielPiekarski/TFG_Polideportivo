namespace WebPolideportivo.Models;

public class Reserva
{
    public int Id { get; set; }
    public int PistaId { get; set; }
    public string NombrePista { get; set; } = "";
    public DateTime Fecha { get; set; }
    public string HoraInicio { get; set; } = "";
    public string HoraFin { get; set; } = "";
    public decimal PrecioTotal { get; set; }
}

