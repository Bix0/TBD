import { useState, useEffect } from 'react';
import axios from 'axios';
import Navbar from '../components/Navbar';

function Inventario() {
  const [itemsInventario, setItemsInventario] = useState([]);
  const [catalogo, setCatalogo] = useState([]);
  
  const idPersonajeActivo = localStorage.getItem('activePersonajeId');
  const token = localStorage.getItem('token');
  const configSeguridad = { headers: { Authorization: `Bearer ${token}` } };

  const cargarDatos = async () => {
    if (!idPersonajeActivo || idPersonajeActivo === "null") return;
    try {
      const resItems = await axios.get('http://localhost:8080/api/items', configSeguridad);
      const resInv = await axios.get(`http://localhost:8080/api/personajes/${idPersonajeActivo}/inventario`, configSeguridad);
      setCatalogo(resItems.data);
      setItemsInventario(resInv.data);
    } catch (error) {
      console.error("Error al cargar inventario:", error);
    }
  };

  useEffect(() => { cargarDatos(); }, [idPersonajeActivo]);

  const manejarEquipar = async (idInventario, estaEquipado) => {
    try {
      //Si vamos a equipar, primero desequipamos lo que ya este puesto
      if (!estaEquipado) {
        const itemPuesto = itemsInventario.find(i => i.equipado === true);
        if (itemPuesto) {
          const idDesequipar = itemPuesto.id_inventario || itemPuesto.idInventario;
          await axios.put(`http://localhost:8080/api/personajes/${idPersonajeActivo}/inventario/${idDesequipar}/desequipar`, null, configSeguridad);
        }
      }

      const accion = estaEquipado ? 'desequipar' : 'equipar';
      await axios.put(`http://localhost:8080/api/personajes/${idPersonajeActivo}/inventario/${idInventario}/${accion}`, null, configSeguridad);
      
      cargarDatos(); // Refrescamos la mochila
    } catch (error) {
      alert("Error al intentar equipar el arma.");
    }
  };

  const tirarObjeto = (idInventario) => {
    if(window.confirm("¿Seguro que deseas botar este ítem? Se perderá para siempre.")) {
      axios.delete(`http://localhost:8080/api/personajes/${idPersonajeActivo}/inventario/${idInventario}`, configSeguridad)
        .then(() => cargarDatos());
    }
  };

  return (
    <div style={{ backgroundColor: '#121212', minHeight: '100vh', color: 'white' }}>
      <Navbar />
      <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
        <h1 style={{ textAlign: 'center', color: '#ff9800', marginBottom: '30px' }}>🎒 Mi Mochila</h1>

        <div style={{ display: 'grid', gap: '15px' }}>
          {(!idPersonajeActivo || idPersonajeActivo === "null") ? (
            <div style={{ textAlign: 'center', padding: '40px', backgroundColor: '#1a1a1a', borderRadius: '8px' }}>
              <p style={{ color: '#f44336' }}>Ningún personaje seleccionado. Ve a "Mis Personajes" y usa uno.</p>
            </div>
          ) : itemsInventario.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '40px', backgroundColor: '#1a1a1a', borderRadius: '8px' }}>
              <p style={{ color: '#888' }}>Tu mochila está vacía.</p>
            </div>
          ) : (
            itemsInventario.map(inv => {
              const dataItem = catalogo.find(c => c.id_item === inv.id_item) || { nombre: 'Ítem Desconocido', item_lvl: 0, ganancia_dkp: 0 };
              
              return (
                <div key={inv.id_inventario} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '20px', backgroundColor: inv.equipado ? '#1e3320' : '#1a1a1a', border: inv.equipado ? '1px solid #4caf50' : '1px solid #444', borderRadius: '8px' }}>
                  <div>
                    <h3 style={{ margin: '0 0 8px 0', color: inv.equipado ? '#81c784' : '#fff' }}>{dataItem.nombre} {inv.equipado && "(Equipado)"}</h3>
                    <div style={{ display: 'flex', gap: '15px' }}>
                      <span style={{ backgroundColor: '#333', padding: '4px 8px', borderRadius: '4px', fontSize: '13px', color: '#ff9800' }}>⚔️ Poder: +{dataItem.item_lvl || dataItem.itemLvl}</span>
                      <span style={{ backgroundColor: '#333', padding: '4px 8px', borderRadius: '4px', fontSize: '13px', color: '#61dafb' }}>💎 Valor DKP: {dataItem.ganancia_dkp || dataItem.gananciaDkp}</span>
                    </div>
                  </div>
                  
                  <div style={{ display: 'flex', gap: '10px' }}>
                    <button onClick={() => manejarEquipar(inv.id_inventario, inv.equipado)} style={{ padding: '10px 20px', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold', backgroundColor: inv.equipado ? '#f44336' : '#4caf50' }}>
                      {inv.equipado ? 'Desequipar' : 'Equipar'}
                    </button>
                    <button onClick={() => tirarObjeto(inv.id_inventario)} style={{ padding: '10px', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', backgroundColor: '#555' }}>🗑️ Botar</button>
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

export default Inventario;