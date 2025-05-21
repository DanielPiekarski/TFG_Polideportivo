using WebPolideportivo.Models;

namespace WebPolideportivo.Services;

public class AuthService
{
    public TokenDto? Token { get; private set; }
    public UserDto? Usuario { get; private set; }

    public bool IsAuthenticated => Token != null;

    public void IniciarSesion(TokenDto token, UserDto usuario)
    {
        Token = token;
        Usuario = usuario;
    }

    public void CerrarSesion()
    {
        Token = null;
        Usuario = null;
    }
}

