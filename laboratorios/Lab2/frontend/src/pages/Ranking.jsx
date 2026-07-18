import { useState, useEffect } from 'react';
import axios from 'axios';
import Navbar from '../components/Navbar';
import MapaClanes from '../components/MapaClanes'; // 1. Importamos el componente del mapa

function Ranking() {
  const [personajes, setPersonajes] = useState([]);
  
  const miUserId = parseInt(localStorage.getItem('userId'));

  useEffect(() => {
    const token = localStorage.getItem('token');
    
    axios.get('http://localhost:8080/api/personajes', {
      headers: { Authorization: `Bearer ${token}` }
    })
      .then(response => {
        const rankingOrdenado = response.data.sort((a, b) => {
           const puntosA = a.puntos_merito || a.puntosMerito || 0;
           const puntosB = b.puntos_merito || b.puntosMerito || 0;
           return puntosB - puntosA;
        });
        setPersonajes(rankingOrdenado);
      })
      .catch(error => console.error("Error cargando el ranking:", error));
  }, []);

  const miIndice = personajes.findIndex(p => 
    p.id_jugador === miUserId || p.idJugador === miUserId || p.jugador?.id === miUserId
  );
  const miPosicionActual = miIndice !== -1 ? miIndice + 1 : null;

  return (
    <div style={{ backgroundColor: '#121212', minHeight: '100vh', color: 'white' }}>
      <Navbar />
      <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
        
        {/* ========================================= */}
        {/* --- MAPA DE CALOR TERRITORIAL (LAB 2) --- */}
        {/* ========================================= */}
        <div style={{ backgroundColor: '#1a1a1a', padding: '20px', borderRadius: '12px', border: '1px solid #aa3bff', marginBottom: '40px' }}>
          <h2 style={{ color: '#aa3bff', marginTop: 0, textAlign: 'left' }}>🔥 Mapa de Calor Territorial</h2>
          <p style={{ color: '#aaa', fontSize: '14px', marginBottom: '20px', textAlign: 'left' }}>
            Visualiza las sedes de poder de los clanes. A mayor DKP, mayor será la influencia territorial.
          </p>
          <MapaClanes />
        </div>

        <h1 style={{ textAlign: 'center', color: '#61dafb', marginBottom: '10px' }}>
          🏆 Ranking DKP del Clan
        </h1>

        {miPosicionActual && (
          <div style={{ textAlign: 'center', marginBottom: '30px', fontSize: '18px', color: '#aaa' }}>
            Tu posición actual es: <strong style={{ color: '#ff9800', fontSize: '24px' }}>#{miPosicionActual}</strong>
          </div>
        )}
        
        <div style={{ backgroundColor: '#1a1a1a', borderRadius: '8px', border: '1px solid #333', overflow: 'hidden' }}>
          <div style={{ display: 'flex', backgroundColor: '#242424', padding: '15px', fontWeight: 'bold', borderBottom: '2px solid #444' }}>
            <div style={{ width: '10%' }}>#</div>
            <div style={{ width: '40%' }}>Personaje</div>
            <div style={{ width: '25%' }}>Clase</div>
            <div style={{ width: '25%', textAlign: 'right', color: '#ff9800' }}>Puntos (DKP)</div>
          </div>

          {personajes.length === 0 ? (
             <div style={{ padding: '20px', textAlign: 'center', color: '#888' }}>Cargando gladiadores...</div>
          ) : (
            personajes.map((personaje, index) => {
              const nombre = personaje.nombre;
              const clase = personaje.clase;
              const puntos = personaje.puntos_merito || personaje.puntosMerito || 0;
              
              const esMiPersonaje = (personaje.id_jugador === miUserId || personaje.idJugador === miUserId || personaje.jugador?.id === miUserId);
              
              let colorPosicion = '#aaa';
              if (index === 0) colorPosicion = '#ffd700';
              if (index === 1) colorPosicion = '#c0c0c0';
              if (index === 2) colorPosicion = '#cd7f32';

              return (
                <div key={personaje.id_personaje || personaje.idPersonaje || index} 
                     style={{ 
                       display: 'flex', 
                       padding: '15px', 
                       borderBottom: '1px solid #333', 
                       alignItems: 'center',
                       backgroundColor: esMiPersonaje ? '#2a2a2a' : 'transparent',
                       borderLeft: esMiPersonaje ? '4px solid #61dafb' : '4px solid transparent'
                     }}>
                  <div style={{ width: '10%', fontSize: '20px', fontWeight: 'bold', color: colorPosicion }}>
                    {index + 1}
                  </div>
                  <div style={{ width: '40%', fontWeight: 'bold', color: esMiPersonaje ? '#61dafb' : '#fff' }}>
                    {nombre} {esMiPersonaje && <span style={{ color: '#ff9800', fontSize: '12px', marginLeft: '5px' }}>(Tú)</span>}
                  </div>
                  <div style={{ width: '25%', color: '#888' }}>{clase}</div>
                  <div style={{ width: '25%', textAlign: 'right', fontWeight: 'bold', color: '#ff9800', fontSize: '18px' }}>
                    {puntos}
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
}

export default Ranking;