import { Link } from "react-router-dom";
import React from 'react';

function Navbar() {
  const cerrarSesion = () => {
    localStorage.clear(); 
    window.location.href = '/'; 
  };

  const rol = localStorage.getItem('rol');

  return (
    <nav style={{ backgroundColor: '#1e1e1e', padding: '15px 30px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '2px solid #333', boxShadow: '0 4px 8px rgba(0,0,0,0.4)', fontFamily: 'Arial, sans-serif' }}>
      
      <div style={{ fontSize: '22px', fontWeight: 'bold', color: '#61dafb' }}>
        ⚔️ Guild Tracker
      </div>

      <div style={{ display: 'flex', gap: '15px', alignItems: 'center' }}>
        <Link to="/personajes" style={{ color: '#81c784', textDecoration: 'none', fontWeight: 'bold' }}>🎭 Mis Personajes</Link>
        <Link to="/raids" style={{ color: '#aaa', textDecoration: 'none', fontWeight: 'bold' }}>Buscador Raids</Link>
        <Link to="/inventario" style={{ color: '#ff9800', textDecoration: 'none', fontWeight: 'bold' }}>🎒 Inventario</Link>
        <Link to="/historial" style={{ color: '#ba68c8', textDecoration: 'none', fontWeight: 'bold' }}>📜 Historial Botín</Link>
        <Link to="/ranking" style={{ color: '#aaa', textDecoration: 'none', fontWeight: 'bold' }}>Ranking DKP</Link>
        <Link to="/facciones" style={{ color: '#aaa', textDecoration: 'none', fontWeight: 'bold' }}>Facciones</Link>
        
        {rol === 'Admin' && (
          <Link to="/admin" style={{ color: '#f44336', textDecoration: 'none', fontWeight: 'bold', marginLeft: '10px', borderLeft: '1px solid #444', paddingLeft: '15px' }}>
            ⚙️ Panel Admin
          </Link>
        )}

        <button onClick={cerrarSesion} style={{ backgroundColor: '#ff4d4d', color: '#fff', border: 'none', padding: '8px 16px', borderRadius: '5px', cursor: 'pointer', fontWeight: 'bold', marginLeft: '15px' }}>
          Salir
        </button>
      </div>
    </nav>
  );
}

export default Navbar;