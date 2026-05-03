import { useState, useEffect } from 'react';
import axios from 'axios';
import Navbar from '../components/Navbar';

function Inventario() {
  const [items, setItems] = useState([]);
  
  // 1. Obtenemos el ID real y el Token directamente del almacenamiento del navegador
  const userId = localStorage.getItem('userId'); 
  const token = localStorage.getItem('token');

  // 2. Preparamos el "Pase VIP" (Header) para que Spring Security no nos bloquee
  const configSeguridad = {
    headers: { Authorization: `Bearer ${token}` }
  };

  const cargarInventario = () => {
    if (!userId || !token) return; // Si no hay usuario, no hacemos la petición

    // 3. Usamos el userId real y le pasamos la configuración de seguridad
    axios.get(`http://localhost:8080/api/personajes/${userId}/inventario`, configSeguridad)
      .then(response => {
        setItems(response.data);
      })
      .catch(error => console.error("Error cargando inventario:", error));
  };

  useEffect(() => {
    cargarInventario();
  }, []);

  const manejarEquipar = (idItemInventario, estaEquipado) => {
    const accion = estaEquipado ? 'desequipar' : 'equipar';
    
    // También enviamos el token al intentar equipar/desequipar
    axios.put(`http://localhost:8080/api/personajes/${userId}/inventario/${idItemInventario}/${accion}`, null, configSeguridad)
      .then(() => {
        cargarInventario(); 
      })
      .catch(error => {
        console.error("Error al modificar el ítem:", error);
        alert("Hubo un error al intentar equipar/desequipar el objeto.");
      });
  };

  return (
    <div style={{ backgroundColor: '#121212', minHeight: '100vh', color: 'white' }}>
      <Navbar />
      <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
        <h1 style={{ textAlign: 'center', color: '#ff9800', marginBottom: '30px' }}>
          🎒 Mi Inventario
        </h1>
        
        <div style={{ display: 'grid', gap: '15px' }}>
          {items.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '40px', backgroundColor: '#1a1a1a', borderRadius: '8px' }}>
              <p style={{ color: '#888', fontSize: '18px' }}>Tu mochila está vacía.</p>
              <p style={{ color: '#555', fontSize: '14px' }}>¡Participa en Raids para conseguir equipo!</p>
            </div>
          ) : (
            items.map((itemObj, index) => {
              const idInventario = itemObj.id_inventario || itemObj.idInventario;
              const nombreObjeto = itemObj.item?.nombre || "Objeto Misterioso";
              const itemLevel = itemObj.item?.item_lvl || itemObj.item?.itemLvl || 0;
              const estaEquipado = itemObj.equipado;

              return (
                <div key={idInventario || index} style={{ 
                  display: 'flex', 
                  justifyContent: 'space-between', 
                  alignItems: 'center',
                  padding: '20px', 
                  backgroundColor: estaEquipado ? '#1e3320' : '#1a1a1a', 
                  border: estaEquipado ? '1px solid #4caf50' : '1px solid #444',
                  borderRadius: '8px',
                  transition: '0.3s'
                }}>
                  <div>
                    <h3 style={{ margin: '0 0 5px 0', color: estaEquipado ? '#81c784' : '#fff' }}>
                      {nombreObjeto} {estaEquipado && " (Equipado)"}
                    </h3>
                    <p style={{ margin: 0, color: '#aaa', fontSize: '14px' }}>
                      Nivel de Objeto (iLvl): <strong style={{ color: '#ff9800' }}>{itemLevel}</strong>
                    </p>
                  </div>
                  
                  <button 
                    onClick={() => manejarEquipar(idInventario, estaEquipado)}
                    style={{ 
                      padding: '10px 20px', 
                      backgroundColor: estaEquipado ? '#f44336' : '#4caf50', 
                      color: 'white', 
                      border: 'none', 
                      borderRadius: '4px',
                      cursor: 'pointer',
                      fontWeight: 'bold',
                      minWidth: '120px'
                    }}
                  >
                    {estaEquipado ? 'Desequipar' : 'Equipar'}
                  </button>
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
}

export default Inventario;