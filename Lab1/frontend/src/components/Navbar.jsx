import { Link, useNavigate } from "react-router-dom";

function Navbar() {
  const navigate = useNavigate();

  const cerrarSesion = () => {
    // En el futuro, aquí borraremos el token JWT guardado
    alert("Cerrando sesión...");
    navigate('/login'); // Te devuelve a la pantalla de inicio
  };

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
        
        {/* Este enlace aún no funciona porque no hemos creado Ranking.jsx, pero lo dejamos listo */}
        <Link to="/ranking" style={{ color: '#aaa', textDecoration: 'none', fontSize: '16px', fontWeight: 'bold' }}>
          Ranking DKP
        </Link>

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