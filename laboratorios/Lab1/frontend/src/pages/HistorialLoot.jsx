import { useState, useEffect } from 'react';
import axios from 'axios';
import Navbar from '../components/Navbar';

function HistorialLoot() {
  const [historial, setHistorial] = useState([]);
  const userId = localStorage.getItem('userId');

  useEffect(() => {
    axios.get(`http://localhost:8080/api/jugadores/${userId}/historial-loot`)
      .then(res => setHistorial(res.data))
      .catch(err => console.error(err));
  }, [userId]);

  return (
    <div style={{ backgroundColor: '#121212', minHeight: '100vh', color: 'white' }}>
      <Navbar />
      <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
        <h1 style={{ textAlign: 'center', color: '#ba68c8', marginBottom: '30px' }}>📜 Mi Historial de Botín (Todos mis Pjs)</h1>
        
        <div style={{ backgroundColor: '#1a1a1a', borderRadius: '8px', border: '1px solid #333', overflow: 'hidden' }}>
          <div style={{ display: 'flex', backgroundColor: '#242424', padding: '15px', fontWeight: 'bold', borderBottom: '2px solid #444' }}>
            <div style={{ width: '25%' }}>Fecha</div>
            <div style={{ width: '25%' }}>Personaje</div>
            <div style={{ width: '30%' }}>Ítem Ganado</div>
            <div style={{ width: '20%' }}>Raid</div>
          </div>
          
          {historial.length === 0 ? (
            <p style={{ textAlign: 'center', padding: '20px', color: '#888' }}>No has ganado ningún botín aún.</p>
          ) : (
            historial.map((fila, i) => (
              <div key={i} style={{ display: 'flex', padding: '15px', borderBottom: '1px solid #333' }}>
                <div style={{ width: '25%', color: '#aaa', fontSize: '14px' }}>{new Date(fila[0]).toLocaleString()}</div>
                <div style={{ width: '25%', color: '#81c784', fontWeight: 'bold' }}>{fila[1]}</div>
                <div style={{ width: '30%', color: '#ff9800' }}>{fila[2]}</div>
                <div style={{ width: '20%', color: '#61dafb' }}>{fila[3]}</div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
export default HistorialLoot;