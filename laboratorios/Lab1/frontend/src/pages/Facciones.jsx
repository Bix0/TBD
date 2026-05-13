import { useState, useEffect } from 'react';
import axios from 'axios';
import Navbar from '../components/Navbar';

function Facciones() {
  const [alianza, setAlianza] = useState([]);
  const [horda, setHorda] = useState([]);
  const [auditoria, setAuditoria] = useState([]);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const configSeguridad = { headers: { Authorization: `Bearer ${token}` } };

    axios.get('http://localhost:8080/api/personajes', configSeguridad)
      .then(response => {
        const todos = response.data.sort((a, b) => (b.puntos_merito || 0) - (a.puntos_merito || 0));
        setAlianza(todos.filter(p => p.faccion === 'Alianza'));
        setHorda(todos.filter(p => p.faccion === 'Horda'));
      })
      .catch(error => console.error("Error:", error));

    // Cargar Auditoria (Trigger 2)
    axios.get('http://localhost:8080/api/clanes/auditoria', configSeguridad)
      .then(res => setAuditoria(res.data))
      .catch(err => console.error(err));
  }, []);

  const renderTabla = (faccionData, color, titulo) => (
    <div style={{ flex: 1, backgroundColor: '#1a1a1a', borderRadius: '8px', border: `1px solid ${color}`, overflow: 'hidden' }}>
      <h2 style={{ textAlign: 'center', backgroundColor: color, color: '#fff', margin: 0, padding: '15px' }}>{titulo}</h2>
      {faccionData.map((p, i) => (
        <div key={p.id_personaje} style={{ display: 'flex', justifyContent: 'space-between', padding: '15px', borderBottom: '1px solid #333' }}>
          <span style={{ fontWeight: 'bold', color: '#fff' }}>{i + 1}. {p.nombre} {i === 0 && '👑 (Líder)'}</span>
          <span style={{ color: '#ff9800', fontWeight: 'bold' }}>{p.puntos_merito || 0} DKP</span>
        </div>
      ))}
    </div>
  );

  return (
    <div style={{ backgroundColor: '#121212', minHeight: '100vh', color: 'white', paddingBottom: '50px' }}>
      <Navbar />
      <div style={{ padding: '20px', maxWidth: '1000px', margin: '0 auto' }}>
        <h1 style={{ textAlign: 'center', color: '#61dafb', marginBottom: '30px' }}>Guerra de Facciones</h1>
        <div style={{ display: 'flex', gap: '30px', flexWrap: 'wrap', marginBottom: '40px' }}>
          {renderTabla(alianza, '#1976d2', '🛡️ LA ALIANZA')}
          {renderTabla(horda, '#d32f2f', '🪓 LA HORDA')}
        </div>

        {/* Auditoria Integrada */}
        <h2 style={{ textAlign: 'center', color: '#4caf50', borderBottom: '2px solid #4caf50', paddingBottom: '10px' }}>📜 Historial de Reyes (Auditoría Liderazgo)</h2>
        <div style={{ backgroundColor: '#1a1a1a', padding: '20px', borderRadius: '8px', border: '1px solid #4caf50' }}>
          <p style={{ color: '#aaa', fontSize: '14px', marginBottom: '15px' }}>
            Si un jugador supera en DKP al Rey actual tras una Raid, asume automáticamente el liderazgo del Clan.
          </p>
          <div style={{ backgroundColor: '#242424', padding: '15px', borderRadius: '6px' }}>
            {auditoria.length === 0 ? <p style={{ fontSize: '14px', color: '#888' }}>Ningún rey ha sido destronado todavía.</p> : null}
            {auditoria.map((fila, i) => (
               <div key={i} style={{ padding: '10px 0', borderBottom: '1px dotted #444', fontSize: '14px' }}>
                 <span style={{ color: '#888' }}>[{new Date(fila[4]).toLocaleString()}]</span> El trono de <strong style={{color:'#61dafb'}}>{fila[1]}</strong> pasó de <strong>{fila[2]}</strong> a manos de <strong>{fila[3]}</strong>.
               </div>
            ))}
          </div>
        </div>

      </div>
    </div>
  );
}

export default Facciones;