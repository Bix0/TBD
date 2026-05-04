import { useState, useEffect } from 'react';
import axios from 'axios';
import RaidFilterBar from '../components/RaidFilterBar';
import Navbar from '../components/Navbar';

function Raids() {
  const [Rolfiltro, setRolfiltro] = useState('Todos');
  const [Ilvfiltro, setIlvfiltro] = useState(0);
  const [raids, setRaids] = useState([]); // Ahora inicia vacío

  // 1. CARGAR DATOS DESDE SPRING BOOT AL INICIAR
  const cargarRaids = () => {
    // Asumiendo que tu Spring Boot corre en el puerto 8080
    axios.get('http://localhost:8080/api/raids')
      .then(response => {
        setRaids(response.data);
        console.log("¡MIRA AQUÍ! Estos son los datos reales:", response.data); // Guardamos el JSON real de la BD
      })
      .catch(error => {
        console.error("Error conectando al backend:", error);
      });
  }; // Los corchetes vacíos indican que solo se ejecuta una vez al cargar la página

  useEffect(() => {
    cargarRaids();
  }, []); // El arreglo vacío asegura que solo se ejecute una vez al montar el componente

  // 2. FUNCIÓN PARA ENVIAR LA SOLICITUD AL BACKEND
  const manejarInscripcion = (idRaid, estadoRaid) => {
    //evitar inscripcion si la raid ya está cerrada
    if (estadoRaid !== 'Programada') {
      alert("No puedes unirte a esta raid, esta raid ya está " + estadoRaid.toLowerCase() + ".");
      return;
    }

    // 2. Adiós al ID quemado. Obtenemos el ID real y el Token del almacenamiento
    const userId = localStorage.getItem('userId');
    const token = localStorage.getItem('token');

    if (!userId || !token) {
      alert("Error: No estás logueado o tu sesión caducó.");
      return;
    }

    // 3. Preparamos el encabezado de seguridad
    const configSeguridad = {
      headers: { Authorization: `Bearer ${token}` }
    };

    // 4. Enviamos la petición dinámica al backend
    // OJO: Usamos null como cuerpo de la petición (body) porque los datos van en la URL y el header
    axios.post(`http://localhost:8080/api/raids/${idRaid}/inscribir?idPersonaje=${userId}`, null, configSeguridad)
      .then(response => {
        alert("¡Inscripción exitosa! Estás en la Raid. Ve a afilar tus armas.");
        // (Opcional) Aquí podríamos volver a cargar las raids para ver cómo bajan los cupos
        cargarRaids();
      })
      .catch(error => {
        // Si PostgreSQL o Spring Boot te rechazan (ej. por Item Level), mostramos el motivo exacto
        alert("Error de inscripción: " + (error.response?.data || "No tienes el Item Level requerido o no hay cupos."));
      });
  };




  return (
    <div style={{ backgroundColor: '#121212', minHeight: '100vh', color: 'white' }}>
      <Navbar /> {/* Agregamos la barra de navegación */}
      <div style={{ padding: '20px' }}>
        <h1 style={{ textAlign: 'center', color: '#61dafb', marginBottom: '20px' }}>
          🏰 Buscador de Raids
        </h1>
        <div style={{ fontFamily: 'arial', maxWidth: '800px', margin: '0 auto' }}>
          <RaidFilterBar
            rolFiltro={Rolfiltro}
            setRolFiltro={setRolfiltro}
            ilvlFiltro={Ilvfiltro}
            setIlvlFiltro={setIlvfiltro}
          />

          <div>
            <p> Raids cargadas desde PostgreSQL: {raids.length} </p>
            <p> Filtro actual {'->'} Rol: {Rolfiltro} | Item Level: {Ilvfiltro} </p>
          </div>

          {/* Grilla de Raids (Adaptada a los nombres de tu Raid.java) */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '20px', marginTop: '30px' }}>




            {raids
              .filter(raid => {
                // 1. LA MATEMÁTICA CORRECTA Y EL NOMBRE CORRECTO
                const cumpleNivel = raid.item_level_requerido >= Ilvfiltro;

                const rolBuscado = Rolfiltro.toLowerCase();
                let cumpleRol = true;

                if (rolBuscado !== 'todos') {
                  // 2. VOLVEMOS A LOS GUIONES BAJOS
                  if (rolBuscado === 'tanque') cumpleRol = raid.cupos_tanque > 0;
                  else if (rolBuscado === 'healer') cumpleRol = raid.cupos_healer > 0;
                  else if (rolBuscado === 'dps') cumpleRol = raid.cupos_dps > 0;
                }

                return cumpleNivel && cumpleRol;
              })
              .map(raid => (
                // 3. VOLVEMOS AL ID CON GUION BAJO
                <div key={raid.id_raid} style={{ border: '1px solid #444', padding: '20px', borderRadius: '8px', backgroundColor: '#1a1a1a', color: 'white' }}>
                  <h3 style={{ margin: '0 0 5px 0', color: '#61dafb' }}>{raid.nombre}</h3>
                  <p style={{ margin: '0 0 15px 0', fontSize: '12px', color: '#888' }}>
                    Estado: {raid.estado}
                  </p>

                  <p style={{ margin: '0 0 15px 0', fontSize: '14px', color: '#aaa' }}>
                    iLvl Requerido: <strong>{raid.item_level_requerido}</strong>
                  </p>

                  <div style={{ backgroundColor: '#242424', padding: '10px', borderRadius: '5px' }}>
                    <p style={{ margin: '5px 0', color: raid.cupos_tanque > 0 ? '#4caf50' : '#f44336' }}>
                      🛡️ Tanques: {raid.cupos_tanque}
                    </p>
                    <p style={{ margin: '5px 0', color: raid.cupos_healer > 0 ? '#4caf50' : '#f44336' }}>
                      ➕ Healers: {raid.cupos_healer}
                    </p>
                    <p style={{ margin: '5px 0', color: raid.cupos_dps > 0 ? '#4caf50' : '#f44336' }}>
                      ⚔️ DPS: {raid.cupos_dps}
                    </p>
                  </div>

                  <button
                    style={{ width: '100%', marginTop: '15px', padding: '10px', cursor: 'pointer', backgroundColor: '#61dafb', color: '#000', border: 'none', borderRadius: '4px', fontWeight: 'bold' }}
                    onClick={() => manejarInscripcion(raid.id_raid, raid.estado)} // Enviamos el ID real de la raid y su estado actual
                  >
                    Solicitar Ingreso
                  </button>
                </div>
              ))
            }



          </div> {/* Fin de la grilla de raids */}
        </div> {/* Fin del contenedor principal */}

      </div>
    </div>
  );
}

export default Raids;
