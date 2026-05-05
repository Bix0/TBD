import React, { useState, useEffect } from 'react';
import axios from 'axios';

function CrearPersonajeModal({ isOpen, onClose }) {
  // Estado para almacenar la lista de clanes disponibles obtenidos desde el backend
  const [clanes, setClanes] = useState([]);
  
  // Estado con los datos del personaje que enviaremos al backend
  const [formData, setFormData] = useState({
    id_jugador: localStorage.getItem('userId') || '', // Se usa la ID de sesión automáticamente
    id_clan: '',
    nombre: '',
    clase: '',
    nivel: 1, // Por defecto todo PJ nuevo es nivel 1
    faccion: '',
    item_level: 0,    // Valor inicial oculto (empieza en 0)
    puntos_merito: 0, // Valor inicial oculto (empieza en 0)
    rol_clan: ''      // Valor inicial oculto (empieza vacío)
  });

  // Hook que se dispara cada vez que cambia el estado "isOpen" (cuando abres el modal)
  useEffect(() => {
    if (isOpen) {
      // Petición para traer la lista de clanes
      axios.get('http://localhost:8080/api/clanes')
        .then(response => {
          setClanes(response.data); // Guardamos la respuesta en el estado 'clanes'
        })
        .catch(error => {
          console.error("Error cargando clanes:", error);
        });
    }
  }, [isOpen]);

  if (!isOpen) return null;

  // Función que actualiza el estado de formData cuando el usuario escribe o selecciona algo
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prevState => ({
      ...prevState,
      [name]: value
    }));
  };

  // Función que se ejecuta al presionar "Crear Personaje" (submit del form)
  const handleSubmit = async (e) => {
    e.preventDefault(); // Evita que la página recargue su vista por defecto
    try {
      const token = localStorage.getItem('token');
      const config = {
        headers: { Authorization: `Bearer ${token}` } // Preparamos la cabecera con el JWT
      };

      const payload = {
        ...formData,
        id_clan: formData.id_clan ? parseInt(formData.id_clan) : null,
      };

      // Se envían los datos del formulario al backend
      await axios.post('http://localhost:8080/api/personajes', payload, config);
      alert('¡Personaje creado con éxito!');
      
      // Avisamos a toda la aplicación que se creó un personaje (para recargar las vistas)
      window.dispatchEvent(new Event('personajeCreado'));
      
      onClose(); // Cierra la ventana modal si todo salió bien
    } catch (error) {
      console.error('Error al crear personaje:', error);
      alert('Error al crear personaje. Revisa la consola para más detalles.');
    }
  };

  return (
    <div style={overlayStyle}>
      <div style={modalStyle}>
        <h2 style={{ color: '#61dafb', borderBottom: '1px solid #444', paddingBottom: '10px' }}>Crear Nuevo Personaje</h2>
        <form onSubmit={handleSubmit} style={formStyle}>

          <div style={fieldStyle}>
            <label>Clan (Opcional):</label>
            <select name="id_clan" value={formData.id_clan} onChange={handleChange} style={{ padding: '8px', borderRadius: '4px', backgroundColor: '#333', color: 'white', border: '1px solid #555' }}>
              <option value="">Sin clan (Ninguno)</option>
              {clanes.map(clan => (
                <option key={clan.id_clan} value={clan.id_clan}>{clan.nombre}</option>
              ))}
            </select>
          </div>

          <div style={fieldStyle}>
            <label>Nombre:</label>
            <input type="text" name="nombre" value={formData.nombre} onChange={handleChange} required />
          </div>

          <div style={fieldStyle}>
            <label>Clase:</label>
            <select name="clase" value={formData.clase} onChange={handleChange} required style={{ padding: '8px', borderRadius: '4px', backgroundColor: '#333', color: 'white', border: '1px solid #555' }}>
              <option value="">Seleccione una clase</option>
              <option value="Tanque">Tanque</option>
              <option value="Healer">Healer</option>
              <option value="DPS">DPS</option>
            </select>
          </div>

          <div style={fieldStyle}>
            <label>Nivel:</label>
            <input type="number" name="nivel" value={formData.nivel} readOnly style={{ padding: '8px', borderRadius: '4px', backgroundColor: '#222', color: '#888', border: '1px solid #444', cursor: 'not-allowed' }} title="Todo personaje inicia en nivel 1" />
          </div>

          {/* Selector para escoger entre Horda o Alianza */}
          <div style={fieldStyle}>
            <label>Facción:</label>
            <select name="faccion" value={formData.faccion} onChange={handleChange} required style={{ padding: '8px', borderRadius: '4px', backgroundColor: '#333', color: 'white', border: '1px solid #555' }}>
              <option value="">Seleccione una facción</option>
              <option value="Horda">Horda</option>
              <option value="Alianza">Alianza</option>
            </select>
          </div>

          {/* Botones de acción del modal */}
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '20px' }}>
            <button type="button" onClick={onClose} style={cancelButtonStyle}>Cancelar</button>
            <button type="submit" style={submitButtonStyle}>Crear Personaje</button>
          </div>
        </form>
      </div>
    </div>
  );
}

// Estilos
const overlayStyle = {
  position: 'fixed',
  top: 0,
  left: 0,
  right: 0,
  bottom: 0,
  backgroundColor: 'rgba(0, 0, 0, 0.7)',
  display: 'flex',
  justifyContent: 'center',
  alignItems: 'center',
  zIndex: 1000
};

const modalStyle = {
  backgroundColor: '#1e1e1e',
  padding: '30px',
  borderRadius: '8px',
  width: '500px',
  maxWidth: '90%',
  color: 'white',
  boxShadow: '0 4px 15px rgba(0,0,0,0.5)',
  maxHeight: '90vh',
  overflowY: 'auto'
};

const formStyle = {
  display: 'flex',
  flexDirection: 'column',
  gap: '15px',
  marginTop: '20px'
};

const fieldStyle = {
  display: 'flex',
  flexDirection: 'column',
  gap: '5px'
};

const submitButtonStyle = {
  backgroundColor: '#4caf50',
  color: 'white',
  border: 'none',
  padding: '10px 15px',
  borderRadius: '4px',
  cursor: 'pointer',
  fontWeight: 'bold'
};

const cancelButtonStyle = {
  backgroundColor: '#f44336',
  color: 'white',
  border: 'none',
  padding: '10px 15px',
  borderRadius: '4px',
  cursor: 'pointer',
  fontWeight: 'bold'
};

export default CrearPersonajeModal;
