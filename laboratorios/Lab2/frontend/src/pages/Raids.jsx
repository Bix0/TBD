import { useState, useEffect } from 'react';
import axios from 'axios';
import RaidFilterBar from '../components/RaidFilterBar';
import Navbar from '../components/Navbar';
import MapaRaids from '../components/MapaRaids'; // Importamos el Mapa

function Raids() {
  const [Rolfiltro, setRolfiltro] = useState('Todos');
  const [Ilvfiltro, setIlvfiltro] = useState(0);
  const [raids, setRaids] = useState([]);
  const [miPersonaje, setMiPersonaje] = useState(null);
  const [inscritos, setInscritos] = useState({});

  const activeId = localStorage.getItem('activePersonajeId');
  const token = localStorage.getItem('token');
  const configSeguridad = { headers: { Authorization: `Bearer ${token}` } };

  const cargarDatos = () => {
    axios.get('http://localhost:8080/api/raids', configSeguridad)
      .then(res => setRaids(res.data))
      .catch(err => console.error("Error cargando raids", err));
      
    if (activeId && activeId !== "null") {
      axios.get(`http://localhost:8080/api/personajes/${activeId}`, configSeguridad)
        .then(res => setMiPersonaje(res.data))
        .catch((err) => {
          if(err.response?.status === 404) {
            localStorage.removeItem('activePersonajeId');
          }
          setMiPersonaje(null);
        });
    }
  };

  useEffect(() => { cargarDatos(); }, [activeId]);

  const cargarInscritos = (idRaid) => {
    axios.get(`http://localhost:8080/api/raids/${idRaid}/inscripciones`, configSeguridad)
      .then(res => setInscritos(prev => ({ ...prev, [idRaid]: res.data })))
      .catch(err => console.error("Error al cargar inscritos", err));
  };

  const manejarInscripcion = (idRaid, estadoRaid) => {
    if (estadoRaid !== 'Programada') return alert("Esta raid ya no acepta inscripciones.");
    if (!miPersonaje) return alert("Ve a 'Mis Personajes' y selecciona 'Usar' en uno de ellos primero.");

    axios.post(`http://localhost:8080/api/raids/${idRaid}/inscribir?idPersonaje=${activeId}`, null, configSeguridad)
      .then((response) => {
        alert(response.data); 
        cargarDatos(); 
        cargarInscritos(idRaid);
      })
      .catch(error => alert("Error: " + (error.response?.data || "No cumples los requisitos o ya estás inscrito.")));
  };

  const manejarDesinscripcion = (idRaid) => {
    if(window.confirm("¿Seguro que deseas abandonar la Raid? Tu cupo será liberado para otro jugador.")) {
        axios.post(`http://localhost:8080/api/raids/${idRaid}/desinscribir?idPersonaje=${activeId}`, null, configSeguridad)
        .then(() => {
            alert("Has abandonado la Raid exitosamente. Cupo recuperado.");
            cargarDatos();
            cargarInscritos(idRaid);
        })
        .catch(error => alert("Error al salir de la raid."));
    }
  };

  return (
    <div style={{ backgroundColor: '#121212', minHeight: '100vh', color: 'white' }}>
      <Navbar />
      <div style={{ padding: '20px', maxWidth: '1000px', margin: '0 auto' }}>
        <h1 style={{ textAlign: 'center', color: '#61dafb', marginBottom: '20px' }}>Buscador de Raids</h1>

        <div style={{ backgroundColor: '#1a1a1a', padding: '15px', borderRadius: '8px', border: '1px solid #4caf50', marginBottom: '20px' }}>
          {miPersonaje ? (
            <>
              <p style={{ margin: 0, color: '#81c784', fontSize: '18px' }}>
                Personaje Activo: <strong>{miPersonaje.nombre}</strong> (Rol: {miPersonaje.rol_clan})
              </p>
              <div style={{ marginTop: '10px', padding: '10px', backgroundColor: '#242424', borderRadius: '5px', borderLeft: '4px solid #ff9800' }}>
                <p style={{ margin: 0, color: '#ff9800', fontSize: '16px' }}>
                  ⚔️ Poder de Combate Total: <strong style={{fontSize: '20px'}}>{miPersonaje.item_level}</strong> 
                </p>
              </div>
            </>
          ) : (
            <p style={{ color: '#f44336', margin: 0 }}>⚠️ No tienes personaje activo. Ve a "Mis Personajes" y presiona "Usar".</p>
          )}
        </div>

        {/* ========================================= */}
        {/* --- MAPA GEOESPACIAL DE RAIDS (LAB 2) --- */}
        {/* ========================================= */}
        <div style={{ backgroundColor: '#1a1a1a', padding: '15px', borderRadius: '8px', border: '1px solid #61dafb', marginBottom: '30px' }}>
          <h2 style={{ color: '#61dafb', marginTop: 0 }}>🗺️ Radar de Raids Cercanas</h2>
          <p style={{ color: '#aaa', fontSize: '14px', marginBottom: '15px' }}>
            Explora el mapa para encontrar los Jefes (Bosses) que han aparecido cerca de tu ubicación.
          </p>
          <MapaRaids />
        </div>

        <RaidFilterBar rolFiltro={Rolfiltro} setRolFiltro={setRolfiltro} ilvlFiltro={Ilvfiltro} setIlvlFiltro={setIlvfiltro} />

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '20px', marginTop: '30px' }}>
          {raids.filter(r => (r.estado === 'Programada' && r.item_level_requerido >= Ilvfiltro && (Rolfiltro === 'Todos' || r[`cupos_${Rolfiltro.toLowerCase()}`] > 0))).map(raid => {
            
            const listadoInscritos = inscritos[raid.id_raid] || [];
            const yoEstoyInscrito = listadoInscritos.some(ins => ins[1] === parseInt(activeId));

            const iLvlInsuficiente = miPersonaje && miPersonaje.item_level < raid.item_level_requerido;
            const sinCupo = miPersonaje && raid[`cupos_${miPersonaje.rol_clan.toLowerCase()}`] <= 0;
            const botonDeshabilitado = !miPersonaje || (iLvlInsuficiente && !yoEstoyInscrito) || (sinCupo && !yoEstoyInscrito);

            return (
              <div key={raid.id_raid} style={{ border: iLvlInsuficiente && !yoEstoyInscrito ? '1px solid #f44336' : yoEstoyInscrito ? '2px solid #4caf50' : '1px solid #444', padding: '20px', borderRadius: '8px', backgroundColor: '#1a1a1a' }}>
                <h3 style={{ color: '#61dafb', margin: '0 0 10px' }}>{raid.nombre} {yoEstoyInscrito && "✅ (Inscrito)"}</h3>
                <p style={{ fontSize: '14px', color: '#aaa' }}>Poder Requerido: <strong>{raid.item_level_requerido}</strong></p>

                <div style={{ backgroundColor: '#242424', padding: '10px', borderRadius: '5px', marginBottom: '15px' }}>
                  <span style={{color: raid.cupos_tanque > 0 ? '#4caf50' : '#f44336'}}>T: {raid.cupos_tanque} </span> | 
                  <span style={{color: raid.cupos_healer > 0 ? '#4caf50' : '#f44336'}}> H: {raid.cupos_healer} </span> | 
                  <span style={{color: raid.cupos_dps > 0 ? '#4caf50' : '#f44336'}}> DPS: {raid.cupos_dps}</span>
                </div>

                <button onClick={() => cargarInscritos(raid.id_raid)} style={{ width: '100%', padding: '8px', marginBottom: '10px', backgroundColor: '#333', color: '#fff', border: '1px solid #555', borderRadius: '4px', cursor: 'pointer' }}>
                  Ver Personajes Inscritos
                </button>
                
                {listadoInscritos.length > 0 && (
                  <div style={{ backgroundColor: '#222', padding: '10px', borderRadius: '4px', marginBottom: '15px', fontSize: '13px' }}>
                    {listadoInscritos.map((ins, i) => (
                      <div key={i} style={{ borderBottom: '1px solid #333', padding: '4px 0', color: ins[1] === parseInt(activeId) ? '#4caf50' : '#aaa' }}>
                        🛡️ {ins[2]} ({ins[3]})
                      </div>
                    ))}
                  </div>
                )}

                {yoEstoyInscrito ? (
                  <button onClick={() => manejarDesinscripcion(raid.id_raid)} style={{ width: '100%', padding: '12px', backgroundColor: '#f44336', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>
                    Abandonar y Liberar Cupo
                  </button>
                ) : (
                  <button disabled={botonDeshabilitado} onClick={() => manejarInscripcion(raid.id_raid, raid.estado)} style={{ width: '100%', padding: '12px', backgroundColor: botonDeshabilitado ? '#444' : '#61dafb', color: botonDeshabilitado ? '#888' : '#000', border: 'none', borderRadius: '4px', cursor: botonDeshabilitado ? 'not-allowed' : 'pointer', fontWeight: 'bold' }}>
                    {iLvlInsuficiente ? 'Poder Insuficiente' : sinCupo ? 'Sin Cupos para tu Rol' : 'Inscribirse a la Batalla'}
                  </button>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}

export default Raids;