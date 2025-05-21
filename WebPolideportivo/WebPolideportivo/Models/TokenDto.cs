using System.Text.Json.Serialization;

namespace WebPolideportivo.Models
{
    public class TokenDto
    {
        [JsonPropertyName("access_token")]
        public string Access_Token { get; set; }

        [JsonPropertyName("token_type")]
        public string Token_Type { get; set; }
    }
}
