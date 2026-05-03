import { useState, useEffect } from 'react';
import axios from 'axios';
import RaidFilterBar from './components/RaidFilterBar';
import Login from './components/Login';

function App() {
  const [user, setUser] = useState(null);       // { token, username, rol, id }
  const [Rolfiltro, setRolfiltro] = useState('Todos');
  const [Ilvfiltro, setIlvfiltro] = useState(0);
  const [raids, setRaids] = useState([]);

  // =========================================================================
  // 1. VERIFICAR SI YA HAY UNA SESIÓN ACTIVA (al recargar la página)
  // =========================================================================
  useEffect(() => {
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    const rol = localStorage.getItem('rol');
    const userId = localStorage.getItem('userId');

    if (token && username) {
      setUser({ token, username, rol, id: parseInt(userId) });
    }
  }, []);

  // =========================================================================
  // 2. CONFIGURAR AXIOS PARA ENVIAR EL JWT EN CADA PETICIÓN
  // =========================================================================
  useEffect(() => {
    // Interceptor: agrega el header Authorization a todas las requests
    const interceptor = axios.interceptors.request.use(config => {
      const token = localStorage.getItem('token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    });

    // Limpiar interceptor al desmontar
    return () => {
      axios.interceptors.request.eject(interceptor);
    };
  }, [user]);

  // =========================================================================
  // 3. CARGAR RAIDS DESDE EL BACKEND (solo si está logueado)
  // =========================================================================
  useEffect(() => {
    if (!user) return;

    axios.get('http://localhost:8080/api/raids')
      .then(response => {
        setRaids(response.data);
      })
      .catch(error => {
        console.error("Error conectando al backend:", error);
      });
  }, [user]); // Recargar cuando cambie el usuario

  // =========================================================================
  // 4. MANEJO DE LOGIN EXITOSO
  // =========================================================================
  const handleLoginSuccess = (userData) => {
    setUser(userData);
  };

  // =========================================================================
  // 5. LOGOUT
  // =========================================================================
  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('rol');
    localStorage.removeItem('userId');
    setUser(null);
    setRaids([]);
  };

  // =========================================================================
  // 6. INSCRIPCIÓN A RAID (con JWT)
  // =========================================================================
  const manejarInscripcion = (idRaid) => {
    // El ID del personaje se obtiene del usuario logueado
    // Por ahora usamos el userId como id_personaje (se puede refinar después)
    const idPersonaje = user?.id || 1;

    axios.post(`http://localhost:8080/api/raids/${idRaid}/inscribir?idPersonaje=${idPersonaje}`)
      .then(response => {
        alert("¡Inscripción exitosa! Estás en la Raid.");
      })
      .catch(error => {
        alert("Error: " + (error.response?.data || "No tienes el Item Level requerido o la raid no existe."));
      });
  };

  // =========================================================================
  // 7. SI NO HAY USUARIO, MOSTRAR LOGIN
  // =========================================================================
  if (!user) {
    return <Login onLoginSuccess={handleLoginSuccess} />;
  }

  // =========================================================================
  // 8. SI HAY USUARIO, MOSTRAR EL DASHBOARD
  // =========================================================================
  return (
    <div style={{ fontFamily: 'arial', maxWidth: '800px', margin: '0 auto' }}>
      {/* Barra superior con info del usuario y Logout */}
      <div style={{
        display: 'flex', justifyContent: 'space-between', alignItems: 'center',
        padding: '15px 0', borderBottom: '1px solid #333', marginBottom: '20px'
      }}>
        <div>
          <span style={{ color: '#61dafb', fontWeight: 'bold' }}>
            {user.username}
          </span>
          <span style={{ color: '#888', marginLeft: '10px', fontSize: '12px' }}>
            ({user.rol})
          </span>
        </div>
        <button
          onClick={handleLogout}
          style={{
            padding: '8px 16px', cursor: 'pointer', backgroundColor: '#f44336',
            color: 'white', border: 'none', borderRadius: '4px', fontWeight: 'bold'
          }}
        >
          Cerrar Sesión
        </button>
      </div>

      <RaidFilterBar
        rolFiltro={Rolfiltro}
        setRolFiltro={setRolfiltro}
        ilvlFiltro={Ilvfiltro}
        setIlvlFiltro={setIlvfiltro}
      />

      <div>
        <p> Raids cargadas desde PostgreSQL: {raids.length} </p>
        <p> Filtro actual {'->'} Rol: {Rolfiltro} | Item Level: {Ilvfiltro} </p>
      </div>

      {/* Grilla de Raids */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '20px', marginTop: '30px' }}>
        {raids
          .filter(raid => {
            const cumpleNivel = Ilvfiltro >= raid.item_level_requerido;
            const rolBuscado = Rolfiltro.toLowerCase();
            let cumpleRol = true;
            if (rolBuscado === 'tanque') cumpleRol = raid.cupos_tanque > 0;
            else if (rolBuscado === 'healer') cumpleRol = raid.cupos_healer > 0;
            else if (rolBuscado === 'dps') cumpleRol = raid.cupos_dps > 0;
            return cumpleNivel && cumpleRol;
          })
          .map(raid => (
            <div key={raid.id_raid} style={{ border: '1px solid #444', padding: '20px', borderRadius: '8px', backgroundColor: '#1a1a1a', color: 'white' }}>
              <h3 style={{ margin: '0 0 5px 0', color: '#61dafb' }}>{raid.nombre}</h3>
              <p style={{ margin: '0 0 15px 0', fontSize: '12px', color: '#888' }}>
                Estado: {raid.estado}
              </p>
              <p style={{ margin: '0 0 15px 0', fontSize: '14px', color: '#aaa' }}>
                iLvl Requerido: <strong>{raid.item_level_requerido}</strong>
              </p>
              <div style={{ backgroundColor: '#242424', padding: '10px', borderRadius: '5px' }}>
                <p style={{ margin: '5px 0', color: raid.cupos_tanque > 0 ? '#4caf50' : '#f44336' }}>
                  🛡️ Tanques: {raid.cupos_tanque}
                </p>
                <p style={{ margin: '5px 0', color: raid.cupos_healer > 0 ? '#4caf50' : '#f44336' }}>
                  ➕ Healers: {raid.cupos_healer}
                </p>
                <p style={{ margin: '5px 0', color: raid.cupos_dps > 0 ? '#4caf50' : '#f44336' }}>
                  ⚔️ DPS: {raid.cupos_dps}
                </p>
              </div>
              <button
                style={{ width: '100%', marginTop: '15px', padding: '10px', cursor: 'pointer', backgroundColor: '#61dafb', color: '#000', border: 'none', borderRadius: '4px', fontWeight: 'bold' }}
                onClick={() => manejarInscripcion(raid.id_raid)}
              >
                Solicitar Ingreso
              </button>
            </div>
          ))
        }
      </div>
    </div>
  );
}

export default App;
