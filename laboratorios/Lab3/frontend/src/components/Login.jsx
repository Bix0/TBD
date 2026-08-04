import { useState } from "react";
import axios from "axios";

function Login({ onLoginSuccess }) {
  const [isRegistering, setIsRegistering] = useState(false);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [msg, setMsg] = useState("");
  const [cargando, setCargando] = useState(false);

  const manejarSubmit = async (e) => {
    e.preventDefault();
    setCargando(true);
    setError("");
    setMsg("");

    try {
      if (isRegistering) {
        await axios.post("/api/auth/register", {
          username: username,
          password: password,
          rol: "Usuario",
        });
        setMsg("Cuenta creada con éxito. Ahora puedes iniciar sesión.");
        setIsRegistering(false);
        setPassword("");
      } else {
        const response = await axios.post("/api/auth/login", {
          username: username,
          password: password,
        });

        const { token, username: user, rol, id } = response.data;
        localStorage.setItem("token", token);
        localStorage.setItem("username", user);
        localStorage.setItem("rol", rol);
        localStorage.setItem("userId", id);

        onLoginSuccess({ token, username: user, rol, id });
      }
    } catch (err) {
      console.error("Error: ", err);
      setError(
        err.response?.data?.error ||
          "Credenciales inválidas o error en el servidor.",
      );
    } finally {
      setCargando(false);
    }
  };

  return (
    <div
      style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        height: "100vh",
        backgroundColor: "#121212",
        color: "white",
        fontFamily: "Arial",
      }}
    >
      <div
        style={{
          backgroundColor: "#1e1e1e",
          padding: "40px",
          borderRadius: "10px",
          boxShadow: "0 4px 8px rgba(0,0,0,0.5)",
          width: "100%",
          maxWidth: "400px",
        }}
      >
        <h2
          style={{
            textAlign: "center",
            color: "#61dafb",
            marginBottom: "30px",
          }}
        >
          🛡️ {isRegistering ? "Crear Cuenta" : "Acceso Guild"}
        </h2>

        {error && (
          <p
            style={{
              color: "#ff4d4d",
              backgroundColor: "#331a1a",
              padding: "10px",
              borderRadius: "5px",
              textAlign: "center",
              fontSize: "14px",
            }}
          >
            {error}
          </p>
        )}
        {msg && (
          <p
            style={{
              color: "#6bff84",
              backgroundColor: "#1f3d24",
              padding: "10px",
              borderRadius: "5px",
              textAlign: "center",
              fontSize: "14px",
            }}
          >
            {msg}
          </p>
        )}

        <form
          onSubmit={manejarSubmit}
          style={{ display: "flex", flexDirection: "column", gap: "15px" }}
        >
          <div>
            <label
              style={{
                display: "block",
                marginBottom: "5px",
                fontSize: "14px",
                color: "#aaa",
              }}
            >
              Usuario:
            </label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              style={{
                width: "100%",
                padding: "10px",
                borderRadius: "5px",
                border: "1px solid #444",
                backgroundColor: "#2a2a2a",
                color: "white",
                boxSizing: "border-box",
              }}
            />
          </div>

          <div>
            <label
              style={{
                display: "block",
                marginBottom: "5px",
                fontSize: "14px",
                color: "#aaa",
              }}
            >
              Contraseña:
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              style={{
                width: "100%",
                padding: "10px",
                borderRadius: "5px",
                border: "1px solid #444",
                backgroundColor: "#2a2a2a",
                color: "white",
                boxSizing: "border-box",
              }}
            />
          </div>

          <button
            type="submit"
            disabled={cargando}
            style={{
              marginTop: "20px",
              padding: "12px",
              backgroundColor: cargando ? "#444" : "#61dafb",
              color: cargando ? "#888" : "#000",
              border: "none",
              borderRadius: "5px",
              fontWeight: "bold",
              cursor: cargando ? "not-allowed" : "pointer",
              transition: "0.3s",
            }}
          >
            {cargando
              ? "Procesando..."
              : isRegistering
                ? "Registrarse"
                : "Entrar al Mundo"}
          </button>

          <p
            style={{
              textAlign: "center",
              marginTop: "15px",
              fontSize: "14px",
              color: "#aaa",
              cursor: "pointer",
              textDecoration: "underline",
            }}
            onClick={() => {
              setIsRegistering(!isRegistering);
              setError("");
              setMsg("");
            }}
          >
            {isRegistering
              ? "¿Ya tienes cuenta? Inicia Sesión"
              : "¿No tienes cuenta? Regístrate aquí"}
          </p>
        </form>
      </div>
    </div>
  );
}

export default Login;
