import { useState, useEffect } from 'react';
import axios from 'axios';
import Navbar from '../components/Navbar';

function Ranking() {
  const [personajes, setPersonajes] = useState([]);
  
  // 1. Obtenemos TU ID de usuario para saber quién eres en la tabla
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

  // 2. Buscamos en qué posición del arreglo (índice) quedaste después de ordenar
  const miIndice = personajes.findIndex(p => 
    p.id_jugador === miUserId || p.idJugador === miUserId || p.jugador?.id === miUserId
  );
  // Si te encontramos (índice distinto a -1), tu posición real es el índice + 1
  const miPosicionActual = miIndice !== -1 ? miIndice + 1 : null;

  return (
    <div style={{ backgroundColor: '#121212', minHeight: '100vh', color: 'white' }}>
      <Navbar />
      <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
        <h1 style={{ textAlign: 'center', color: '#61dafb', marginBottom: '10px' }}>
          🏆 Ranking DKP del Clan
        </h1>

        {/* 3. BANNER DE MOTIVACIÓN: Solo se muestra si el usuario tiene un personaje */}
        {miPosicionActual && (
          <div style={{ textAlign: 'center', marginBottom: '30px', fontSize: '18px', color: '#aaa' }}>
            Tu posición actual es: <strong style={{ color: '#ff9800', fontSize: '24px' }}>#{miPosicionActual}</strong>
          </div>
        )}
        
        <div style={{ backgroundColor: '#1a1a1a', borderRadius: '8px', border: '1px solid #333', overflow: 'hidden' }}>
          {/* Cabecera de la tabla */}
          <div style={{ display: 'flex', backgroundColor: '#242424', padding: '15px', fontWeight: 'bold', borderBottom: '2px solid #444' }}>
            <div style={{ width: '10%' }}>#</div>
            <div style={{ width: '40%' }}>Personaje</div>
            <div style={{ width: '25%' }}>Clase</div>
            <div style={{ width: '25%', textAlign: 'right', color: '#ff9800' }}>Puntos (DKP)</div>
          </div>

          {/* Filas de personajes */}
          {personajes.length === 0 ? (
             <div style={{ padding: '20px', textAlign: 'center', color: '#888' }}>Cargando gladiadores...</div>
          ) : (
            personajes.map((personaje, index) => {
              const nombre = personaje.nombre;
              const clase = personaje.clase;
              const puntos = personaje.puntos_merito || personaje.puntosMerito || 0;
              
              // 4. Verificamos si esta fila que se está dibujando es la tuya
              const esMiPersonaje = (personaje.id_jugador === miUserId || personaje.idJugador === miUserId || personaje.jugador?.id === miUserId);
              
              let colorPosicion = '#aaa';
              if (index === 0) colorPosicion = '#ffd700'; // Oro
              if (index === 1) colorPosicion = '#c0c0c0'; // Plata
              if (index === 2) colorPosicion = '#cd7f32'; // Bronce

              return (
                <div key={personaje.id_personaje || personaje.idPersonaje || index} 
                     style={{ 
                       display: 'flex', 
                       padding: '15px', 
                       borderBottom: '1px solid #333', 
                       alignItems: 'center',
                       // Si es tu personaje, pintamos el fondo ligeramente más claro y le ponemos un borde izquierdo
                       backgroundColor: esMiPersonaje ? '#2a2a2a' : 'transparent',
                       borderLeft: esMiPersonaje ? '4px solid #61dafb' : '4px solid transparent'
                     }}>
                  
                  <div style={{ width: '10%', fontSize: '20px', fontWeight: 'bold', color: colorPosicion }}>
                    {index + 1}
                  </div>
                  
                  <div style={{ width: '40%', fontWeight: 'bold', color: esMiPersonaje ? '#61dafb' : '#fff' }}>
                    {nombre} {esMiPersonaje && <span style={{ color: '#ff9800', fontSize: '12px', marginLeft: '5px' }}>(Tú)</span>}
                  </div>
                  
                  <div style={{ width: '25%', color: '#888' }}>
                    {clase}
                  </div>
                  
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