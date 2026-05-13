import { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(false); // Corrección 1: Corchetes []

  const navigate = useNavigate(); // Corrección 2: Inicializar el hook

  const manejarSubmit = async (e) => {
    e.preventDefault();
    setCargando(true);
    setError("");

    try {
      /*
      const response = await axios.post("http://localhost:8080/api/auth/login", {
        username: username,
        password: password,
      });
      */

      await new Promise(resolve => setTimeout(resolve, 1000)); // Simula un retraso de 1 segundo
      
      // Corrección 3: Comentamos el uso de "response" porque no existe temporalmente
      // console.log("Respuesta del servidor: ", response.data);
      // alert("Bienvenido, " + response.data.username + "! Tu token JWT es: " + response.data.token);

      navigate('/raids'); // Corrección 2: Usar la variable en minúscula

    } catch (error) {
      console.error("Error al iniciar sesión: ", error);
      setError("Credenciales inválidas. Por favor, intenta de nuevo.");
    } finally {
      setCargando(false);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', backgroundColor: '#121212', color: 'white', fontFamily: 'Arial' }}>
      <div style={{ backgroundColor: '#1e1e1e', padding: '40px', borderRadius: '10px', boxShadow: '0 4px 8px rgba(0,0,0,0.5)', width: '100%', maxWidth: '400px' }}>
        
        <h2 style={{ textAlign: 'center', color: '#61dafb', marginBottom: '30px' }}>
          🛡️ Acceso Guild
        </h2>

        {error && <p style={{ color: '#ff4d4d', backgroundColor: '#331a1a', padding: '10px', borderRadius: '5px', textAlign: 'center', fontSize: '14px' }}>{error}</p>}

        <form onSubmit={manejarSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
          
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontSize: '14px', color: '#aaa' }}>Usuario:</label>
            <input 
              type="text" 
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              style={{ width: '100%', padding: '10px', borderRadius: '5px', border: '1px solid #444', backgroundColor: '#2a2a2a', color: 'white', boxSizing: 'border-box' }}
            />
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontSize: '14px', color: '#aaa' }}>Contraseña:</label>
            <input 
              type="password" 
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              style={{ width: '100%', padding: '10px', borderRadius: '5px', border: '1px solid #444', backgroundColor: '#2a2a2a', color: 'white', boxSizing: 'border-box' }}
            />
          </div>

          <button 
            type="submit" 
            disabled={cargando}
            style={{ marginTop: '20px', padding: '12px', backgroundColor: cargando ? '#444' : '#61dafb', color: cargando ? '#888' : '#000', border: 'none', borderRadius: '5px', fontWeight: 'bold', cursor: cargando ? 'not-allowed' : 'pointer', transition: '0.3s' }}
          >
            {cargando ? 'Conectando...' : 'Entrar al Mundo'}
          </button>
        </form>

      </div>
    </div>
  );
}

export default Login;