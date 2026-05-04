import { useState } from 'react';
import axios from 'axios';
import Navbar from '../components/Navbar';

function PanelAdmin() {
  const rol = localStorage.getItem('rol');

  // Estado del formulario con los nombres de variables exactos del backend
  const [raidData, setRaidData] = useState({
    nombre: '',
    fecha: '',
    item_level_requerido: 0,
    cupos_tanque: 0,
    cupos_healer: 0,
    cupos_dps: 0
  });

  // 1. BARRERA DE SEGURIDAD FRONTEND
  if (rol !== 'Admin') {
    return (
      <div style={{ backgroundColor: '#121212', minHeight: '100vh', color: 'white' }}>
        <Navbar />
        <div style={{ textAlign: 'center', marginTop: '100px' }}>
          <h1 style={{ color: '#f44336', fontSize: '40px' }}>⛔ Acceso Restringido</h1>
          <p style={{ color: '#888', fontSize: '18px' }}>Solo los líderes del clan pueden acceder a la Sala de Mando.</p>
        </div>
      </div>
    );
  }

  // Maneja los cambios en los inputs
  const handleChange = (e) => {
    const { name, value } = e.target;
    setRaidData({ ...raidData, [name]: value });
  };

  // Envía la nueva raid al backend
  const handleSubmit = (e) => {
    e.preventDefault();
    const token = localStorage.getItem('token');

    // Preparamos el objeto sumando el estado por defecto
    const nuevaRaid = {
      ...raidData,
      estado: 'Programada' // Siempre nacen como programadas
    };

    axios.post('http://localhost:8080/api/raids', nuevaRaid, {
      headers: { Authorization: `Bearer ${token}` }
    })
      .then(response => {
        alert("¡Raid creada exitosamente! Ya está disponible en el Buscador.");
        // Limpiamos el formulario
        setRaidData({ nombre: '', fecha: '', item_level_requerido: 0, cupos_tanque: 0, cupos_healer: 0, cupos_dps: 0 });
      })
      .catch(error => {
        console.error(error);
        alert("Error al crear la raid. Revisa la consola.");
      });
  };

  return (
    <div style={{ backgroundColor: '#121212', minHeight: '100vh', color: 'white' }}>
      <Navbar />
      <div style={{ padding: '20px', maxWidth: '600px', margin: '0 auto' }}>
        <h1 style={{ textAlign: 'center', color: '#f44336', marginBottom: '20px' }}>
          ⚙️ Crear Nueva Raid
        </h1>
        
        <form onSubmit={handleSubmit} style={{ backgroundColor: '#1a1a1a', padding: '30px', borderRadius: '8px', border: '1px solid #333' }}>
          
          <div style={{ marginBottom: '15px' }}>
            <label style={{ display: 'block', color: '#aaa', marginBottom: '5px' }}>Nombre de la Raid:</label>
            <input type="text" name="nombre" value={raidData.nombre} onChange={handleChange} required
              style={{ width: '100%', padding: '10px', borderRadius: '4px', border: 'none', backgroundColor: '#333', color: 'white' }} 
              placeholder="Ej: Ciudadela de la Corona de Hielo" />
          </div>

          <div style={{ marginBottom: '15px' }}>
            <label style={{ display: 'block', color: '#aaa', marginBottom: '5px' }}>Fecha y Hora:</label>
            <input type="datetime-local" name="fecha" value={raidData.fecha} onChange={handleChange} required
              style={{ width: '100%', padding: '10px', borderRadius: '4px', border: 'none', backgroundColor: '#333', color: 'white' }} />
          </div>

          <div style={{ marginBottom: '15px' }}>
            <label style={{ display: 'block', color: '#aaa', marginBottom: '5px' }}>Item Level Requerido:</label>
            <input type="number" name="item_level_requerido" value={raidData.item_level_requerido} onChange={handleChange} required min="0"
              style={{ width: '100%', padding: '10px', borderRadius: '4px', border: 'none', backgroundColor: '#333', color: 'white' }} />
          </div>

          {/* Grilla para los cupos */}
          <div style={{ display: 'flex', gap: '15px', marginBottom: '25px' }}>
            <div style={{ flex: 1 }}>
              <label style={{ display: 'block', color: '#aaa', marginBottom: '5px' }}>🛡️ Tanques:</label>
              <input type="number" name="cupos_tanque" value={raidData.cupos_tanque} onChange={handleChange} required min="0"
                style={{ width: '100%', padding: '10px', borderRadius: '4px', border: 'none', backgroundColor: '#333', color: 'white' }} />
            </div>
            <div style={{ flex: 1 }}>
              <label style={{ display: 'block', color: '#aaa', marginBottom: '5px' }}>➕ Healers:</label>
              <input type="number" name="cupos_healer" value={raidData.cupos_healer} onChange={handleChange} required min="0"
                style={{ width: '100%', padding: '10px', borderRadius: '4px', border: 'none', backgroundColor: '#333', color: 'white' }} />
            </div>
            <div style={{ flex: 1 }}>
              <label style={{ display: 'block', color: '#aaa', marginBottom: '5px' }}>⚔️ DPS:</label>
              <input type="number" name="cupos_dps" value={raidData.cupos_dps} onChange={handleChange} required min="0"
                style={{ width: '100%', padding: '10px', borderRadius: '4px', border: 'none', backgroundColor: '#333', color: 'white' }} />
            </div>
          </div>

          <button type="submit" style={{ width: '100%', padding: '12px', backgroundColor: '#f44336', color: 'white', border: 'none', borderRadius: '4px', fontWeight: 'bold', cursor: 'pointer', fontSize: '16px' }}>
            Publicar Evento
          </button>
        </form>
      </div>
    </div>
  );
}

export default PanelAdmin;