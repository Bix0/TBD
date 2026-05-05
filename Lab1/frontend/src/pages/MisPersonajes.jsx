import React, { useState, useEffect } from 'react';
import axios from 'axios';
import Navbar from '../components/Navbar';

function MisPersonajes() {
  const [personajes, setPersonajes] = useState([]);
  const [clanes, setClanes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activePersonajeId, setActivePersonajeId] = useState(localStorage.getItem('activePersonajeId') || null);

  const handleSetActive = (pj) => {
    const idStr = String(pj.id_personaje || pj.idPersonaje || pj.nombre);
    localStorage.setItem('activePersonajeId', idStr);
    localStorage.setItem('activePersonajeNombre', pj.nombre);
    setActivePersonajeId(idStr);
    
    // Disparamos evento para que Inventario o Navbar se enteren si es necesario
    window.dispatchEvent(new Event('personajeActivoCambiado'));
  };

  const cargarPersonajes = () => {
    const userId = localStorage.getItem('userId');
    const token = localStorage.getItem('token');

    if (userId && token) {
      axios.get(`http://localhost:8080/api/personajes/jugador/${userId}`, {
        headers: { Authorization: `Bearer ${token}` }
      })
      .then(response => {
        // Ahora el backend devuelve una lista (200 OK) o nada (204 No Content)
        if (response.status === 200 && Array.isArray(response.data)) {
          setPersonajes(response.data);
        } else {
          setPersonajes([]);
        }
      })
      .catch(error => {
        console.error("Error cargando personajes:", error);
      })
      .finally(() => {
        setLoading(false);
      });
    } else {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarPersonajes();

    // Cargar la lista de clanes para poder mostrar el nombre del clan en vez del ID
    axios.get('http://localhost:8080/api/clanes')
      .then(response => setClanes(response.data))
      .catch(error => console.error("Error cargando clanes:", error));

    // Escuchamos el evento global disparado por el Modal
    window.addEventListener('personajeCreado', cargarPersonajes);
    
    // Limpiamos la escucha al salir de la página
    return () => {
      window.removeEventListener('personajeCreado', cargarPersonajes);
    };
  }, []);

  return (
    <div style={{ backgroundColor: '#121212', minHeight: '100vh', color: 'white' }}>
      <Navbar />
      <div style={{ padding: '20px', width: '95%', maxWidth: '1400px', margin: '0 auto' }}>
        <h1 style={{ textAlign: 'center', color: '#4caf50', marginBottom: '30px' }}>
          🤡 Mis Personajes
        </h1>

        {loading ? (
          <p style={{ textAlign: 'center', color: '#aaa' }}>Cargando tus personajes...</p>
        ) : personajes.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '40px', backgroundColor: '#1a1a1a', borderRadius: '8px' }}>
            <p style={{ color: '#888', fontSize: '18px' }}>Aún no tienes personajes creados.</p>
            <p style={{ color: '#555', fontSize: '14px' }}>¡Crea uno desde la barra superior para comenzar tu aventura!</p>
          </div>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '20px' }}>
            {personajes.map(pj => {
              const pjId = pj.id_personaje || pj.idPersonaje;
              const clanId = pj.id_clan || pj.idClan;
              const clanObj = clanes.find(c => (c.id_clan === clanId) || (c.idClan === clanId));
              const nombreClan = clanObj ? clanObj.nombre : (clanId ? `Clan ID: ${clanId}` : 'Sin Clan');

              const isActivo = activePersonajeId === String(pjId || pj.nombre);

              return (
              <div key={pjId || pj.nombre} style={{ 
                border: isActivo ? '2px solid #4caf50' : '1px solid #444', 
                padding: '20px', 
                borderRadius: '8px', 
                backgroundColor: isActivo ? '#1e3320' : '#1a1a1a', 
                transition: 'all 0.3s',
                cursor: 'default'
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                  <h3 style={{ margin: 0, color: '#61dafb', fontSize: '22px' }}>{pj.nombre}</h3>
                  {isActivo ? (
                    <span style={{ backgroundColor: '#4caf50', color: 'white', padding: '4px 8px', borderRadius: '4px', fontSize: '12px', fontWeight: 'bold' }}>ACTIVO</span>
                  ) : (
                    <button onClick={() => handleSetActive(pj)} style={{ backgroundColor: '#333', color: 'white', border: '1px solid #555', padding: '6px 10px', borderRadius: '4px', cursor: 'pointer', fontSize: '12px', fontWeight: 'bold' }}>Marcar Activo</button>
                  )}
                </div>
                
                <div style={{ backgroundColor: '#242424', padding: '10px', borderRadius: '5px' }}>
                  <p style={{ margin: '5px 0', color: '#aaa' }}>
                    <strong>Clase:</strong> {pj.clase}
                  </p>
                  <p style={{ margin: '5px 0', color: '#aaa' }}>
                    <strong>Nivel:</strong> <span style={{ color: '#ff9800' }}>{pj.nivel}</span>
                  </p>
                  <p style={{ margin: '5px 0', color: '#aaa' }}>
                    <strong>Facción:</strong> <span style={{ color: pj.faccion === 'Horda' ? '#f44336' : pj.faccion === 'Alianza' ? '#2196f3' : '#aaa' }}>{pj.faccion}</span>
                  </p>
                  <p style={{ margin: '5px 0', color: '#aaa' }}>
                    <strong>iLvl:</strong> <span style={{ color: '#4caf50' }}>{pj.item_level || pj.itemLevel || 0}</span>
                  </p>
                  <div style={{ margin: '5px 0', color: '#aaa', display: 'flex', justifyContent: 'space-between' }}>
                    <span><strong>Clan:</strong> <span style={{ color: '#e91e63' }}>{nombreClan}</span></span>
                    <span><strong>DKP:</strong> <span style={{ color: '#ffeb3b' }}>{pj.puntos_merito || pj.puntosMerito || 0}</span></span>
                  </div>
                </div>
              </div>
            )})}
          </div>
        )}
      </div>
    </div>
  );
}

export default MisPersonajes;
