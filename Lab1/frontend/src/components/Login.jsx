import { useState } from 'react';
import axios from 'axios';

/**
 * Pantalla de Login.
 * Envía username + password a POST /api/auth/login
 * y guarda el JWT en localStorage si es exitoso.
 */
function Login({ onLoginSuccess }) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        axios.post('http://localhost:8080/api/auth/login', { username, password })
            .then(response => {
                const { token, username: user, rol, id } = response.data;
                // Guardar el token y datos del usuario en localStorage
                localStorage.setItem('token', token);
                localStorage.setItem('username', user);
                localStorage.setItem('rol', rol);
                localStorage.setItem('userId', id);
                // Notificar al padre que el login fue exitoso
                onLoginSuccess({ token, username: user, rol, id });
            })
            .catch(error => {
                const msg = error.response?.data?.error || 'Error de conexión con el servidor';
                setError(msg);
            })
            .finally(() => setLoading(false));
    };

    return (
        <div style={{
            display: 'flex', justifyContent: 'center', alignItems: 'center',
            minHeight: '100vh', background: '#0d1117', color: 'white'
        }}>
            <form onSubmit={handleSubmit} style={{
                background: '#161b22', padding: '40px', borderRadius: '12px',
                width: '350px', boxShadow: '0 0 20px rgba(0,0,0,0.5)'
            }}>
                <h2 style={{ textAlign: 'center', marginBottom: '30px', color: '#61dafb' }}>
                    🎮 MMORPG - Login
                </h2>

                {error && (
                    <div style={{
                        background: '#3d1f1f', color: '#ff6b6b', padding: '10px',
                        borderRadius: '6px', marginBottom: '15px', textAlign: 'center',
                        fontSize: '14px'
                    }}>
                        {error}
                    </div>
                )}

                <div style={{ marginBottom: '20px' }}>
                    <label style={{ display: 'block', marginBottom: '6px', fontSize: '14px', color: '#aaa' }}>
                        Usuario
                    </label>
                    <input
                        type="text"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        placeholder="bruno"
                        required
                        style={{
                            width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #444',
                            background: '#0d1117', color: 'white', fontSize: '16px', boxSizing: 'border-box'
                        }}
                    />
                </div>

                <div style={{ marginBottom: '25px' }}>
                    <label style={{ display: 'block', marginBottom: '6px', fontSize: '14px', color: '#aaa' }}>
                        Contraseña
                    </label>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="123456"
                        required
                        style={{
                            width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #444',
                            background: '#0d1117', color: 'white', fontSize: '16px', boxSizing: 'border-box'
                        }}
                    />
                </div>

                <button
                    type="submit"
                    disabled={loading}
                    style={{
                        width: '100%', padding: '12px', borderRadius: '6px', border: 'none',
                        background: loading ? '#4a7a8f' : '#61dafb',
                        color: '#000', fontSize: '16px', fontWeight: 'bold', cursor: loading ? 'not-allowed' : 'pointer'
                    }}
                >
                    {loading ? 'Ingresando...' : 'Ingresar'}
                </button>

                <p style={{ textAlign: 'center', marginTop: '20px', fontSize: '12px', color: '#666' }}>
                    Usuarios de prueba: bruno / 123456 (Admin) — xhuala / 123456 (Usuario)
                </p>
            </form>
        </div>
    );
}

export default Login;
