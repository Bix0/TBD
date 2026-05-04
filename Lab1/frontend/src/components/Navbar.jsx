import { Link, useNavigate } from "react-router-dom";
import React from 'react';

function Navbar() {
  const navigate = useNavigate();

  const cerrarSesion = () => {
    // 1. Destruimos las credenciales reales
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('rol');
    localStorage.removeItem('userId');
    
    // 2. Forzamos la recarga de la página para que App.jsx nos devuelva al Login
    window.location.href = '/'; 
  };

  const rol = localStorage.getItem('rol');

  return (
    <nav style={{
      backgroundColor: '#1e1e1e',
      padding: '15px 30px',
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      borderBottom: '2px solid #333',
      boxShadow: '0 4px 8px rgba(0,0,0,0.4)',
      fontFamily: 'Arial, sans-serif'
    }}>
      {/* Logo o Título a la izquierda */}
      <div style={{ fontSize: '22px', fontWeight: 'bold', color: '#61dafb' }}>
        ⚔️ Guild Tracker
      </div>

      {/* Botones de navegación a la derecha */}
      <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
        <Link to="/raids" style={{ color: '#aaa', textDecoration: 'none', fontSize: '16px', fontWeight: 'bold' }}>
          Buscador de Raids
        </Link>
        
        <Link to="/ranking" style={{ color: '#aaa', textDecoration: 'none', fontSize: '16px', fontWeight: 'bold' }}>
          Ranking DKP
        </Link>

        {/* Movimos el Inventario ANTES del botón de desconectar */}
        <Link to="/inventario" style={{ color: '#ff9800', textDecoration: 'none', fontSize: '16px', fontWeight: 'bold' }}>
          🎒 Mi Inventario
        </Link>

        {/* Renderizado Condicional: Solo se dibuja si el rol es 'Admin' */}
        {rol === 'Admin' && (
          <Link to="/admin" style={{ color: '#f44336', textDecoration: 'none', fontSize: '16px', fontWeight: 'bold' }}>
            ⚙️ Panel Admin
          </Link>
        )}

        <button 
          onClick={cerrarSesion}
          style={{
            backgroundColor: '#ff4d4d', 
            color: '#fff', 
            border: 'none', 
            padding: '8px 16px', 
            borderRadius: '5px', 
            cursor: 'pointer', 
            fontWeight: 'bold',
            marginLeft: '15px'
          }}
        >
          Desconectar
        </button>
      </div>
    </nav>
  );
}

export default Navbar;