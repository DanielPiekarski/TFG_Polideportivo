using WebPolideportivo.Models;

namespace WebPolideportivo.Services;

public class ReservaService
{
    public List<Reserva> Reservas { get; set; } = new();

    public void AgregarReserva(Reserva reserva)
    {
        Reservas.Add(reserva);
    }
    public List<Reserva> ObtenerReservas() => Reservas;
}

