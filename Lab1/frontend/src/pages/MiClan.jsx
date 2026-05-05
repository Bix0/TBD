import React, { useState, useEffect } from 'react';
import axios from 'axios';
import Navbar from '../components/Navbar';

function MiClan() {
  const [miembros, setMiembros] = useState([]);
  const [clanInfo, setClanInfo] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [nuevoNombreClan, setNuevoNombreClan] = useState('');
  const [nuevoMiembroId, setNuevoMiembroId] = useState('');
  const [personajesSinClan, setPersonajesSinClan] = useState([]);

  const activePersonajeId = localStorage.getItem('activePersonajeId');
  const token = localStorage.getItem('token');

  const config = { headers: { Authorization: `Bearer ${token}` } };

  const cargarDatosClan = () => {
    if (!activePersonajeId || !token) {
      setLoading(false);
      return;
    }
    // 1. Obtener el personaje activo para saber su ID de clan
    axios.get(`http://localhost:8080/api/personajes/${activePersonajeId}`, config)
      .then(resPj => {
        const clanId = resPj.data.id_clan || resPj.data.idClan;
        
        if (!clanId) {
          setError('Tu personaje activo no pertenece a ningún clan.');
          setLoading(false);
          return;
        }

        // 2. Si tiene clan, obtenemos los detalles del clan y la lista de sus miembros
        const pClan = axios.get(`http://localhost:8080/api/clanes/${clanId}`, config);
        const pMiembros = axios.get(`http://localhost:8080/api/personajes/clan/${clanId}`, config);
        const pTodos = axios.get(`http://localhost:8080/api/personajes`, config);

        Promise.all([pClan, pMiembros, pTodos])
          .then(([resClan, resMiembros, resTodos]) => {
            setClanInfo(resClan.data);
            setMiembros(resMiembros.data);
            
            const sinClan = resTodos.data.filter(pj => !pj.id_clan && !pj.idClan);
            setPersonajesSinClan(sinClan);
            if (sinClan.length > 0) {
              setNuevoMiembroId(sinClan[0].id_personaje || sinClan[0].idPersonaje);
            } else {
              setNuevoMiembroId('');
            }

            setLoading(false);
          })
          .catch(err => {
            console.error("Error cargando datos del clan", err);
            setError('Error al cargar la información del clan de tu personaje.');
            setLoading(false);
          });

      })
      .catch(err => {
        console.error("Error obteniendo personaje activo", err);
        setError('Error al obtener la información de tu personaje activo.');
        setLoading(false);
      });
  };

  useEffect(() => {
    cargarDatosClan();
  }, [activePersonajeId, token]);

  const liderId = clanInfo ? (clanInfo.id_lider || clanInfo.idLider) : null;
  const isLeader = liderId && (activePersonajeId === liderId.toString());

  const handleRenombrarClan = () => {
    if (!nuevoNombreClan) return;
    const datosActualizados = { ...clanInfo, nombre: nuevoNombreClan };
    axios.put(`http://localhost:8080/api/clanes/${clanInfo.id_clan || clanInfo.idClan}`, datosActualizados, config)
      .then(() => {
        alert('Nombre del clan actualizado');
        cargarDatosClan();
      })
      .catch(err => alert('Error al renombrar el clan'));
  };

  const handleAnadirMiembro = () => {
    if (!nuevoMiembroId) return;
    axios.post(`http://localhost:8080/api/clanes/${clanInfo.id_clan || clanInfo.idClan}/miembros?idPersonaje=${nuevoMiembroId}`, null, config)
      .then(() => {
        alert('Miembro añadido exitosamente');
        cargarDatosClan();
      })
      .catch(err => alert('Error al añadir miembro: ' + (err.response?.data || '')));
  };

  const handleExpulsarMiembro = (idPersonaje) => {
    if (window.confirm("¿Seguro que deseas expulsar a este miembro?")) {
      axios.delete(`http://localhost:8080/api/clanes/${clanInfo.id_clan || clanInfo.idClan}/miembros/${idPersonaje}`, config)
        .then(() => {
          alert('Miembro expulsado');
          cargarDatosClan();
        })
        .catch(err => alert('Error al expulsar miembro: ' + (err.response?.data || '')));
    }
  };

  const handleAsignarLider = (idPersonaje) => {
    if (window.confirm("¿Estás seguro? Perderás el liderazgo del clan.")) {
      axios.put(`http://localhost:8080/api/clanes/${clanInfo.id_clan || clanInfo.idClan}/lider?nuevoLider=${idPersonaje}`, null, config)
        .then(() => {
          alert('Liderazgo transferido exitosamente');
          cargarDatosClan();
        })
        .catch(err => alert('Error al transferir liderazgo'));
    }
  };

  const handleCambiarRol = (pj, nuevoRol) => {
    const pjActualizado = { ...pj, rol_clan: nuevoRol };
    axios.put(`http://localhost:8080/api/personajes/${pj.id_personaje || pj.idPersonaje}`, pjActualizado, config)
      .then(() => {
        cargarDatosClan();
      })
      .catch(err => alert('Error al actualizar el rol'));
  };

  return (
    <div style={{ backgroundColor: '#121212', minHeight: '100vh', color: 'white' }}>
      <Navbar />
      <div style={{ padding: '20px', width: '95%', maxWidth: '1400px', margin: '0 auto' }}>
        <h1 style={{ textAlign: 'center', color: '#e91e63', marginBottom: '30px' }}>
          🏰 Mi Clan {clanInfo && `- ${clanInfo.nombre}`}
        </h1>

        {!activePersonajeId ? (
          <div style={{ textAlign: 'center', padding: '40px', backgroundColor: '#1a1a1a', borderRadius: '8px' }}>
            <p style={{ color: '#888', fontSize: '18px' }}>No has seleccionado un personaje activo.</p>
            <p style={{ color: '#555', fontSize: '14px' }}>Ve a "Mis Personajes" y marca uno como Activo.</p>
          </div>
        ) : loading ? (
          <p style={{ textAlign: 'center', color: '#aaa' }}>Cargando datos del clan...</p>
        ) : error ? (
          <div style={{ textAlign: 'center', padding: '40px', backgroundColor: '#1a1a1a', borderRadius: '8px' }}>
             <p style={{ color: '#f44336', fontSize: '18px' }}>{error}</p>
          </div>
        ) : (
          <>
            {isLeader && (
              <div style={{ backgroundColor: '#1e1e1e', padding: '20px', borderRadius: '8px', marginBottom: '20px', border: '1px solid #ffd700' }}>
                <h3 style={{ color: '#ffd700', marginTop: 0 }}>👑 Panel de Administración del Clan</h3>
                <div style={{ display: 'flex', gap: '20px', flexWrap: 'wrap' }}>
                  
                  <div style={{ flex: 1, minWidth: '250px' }}>
                    <p style={{ margin: '0 0 5px 0', color: '#aaa' }}>Cambiar Nombre del Clan</p>
                    <div style={{ display: 'flex', gap: '10px' }}>
                      <input 
                        type="text" 
                        placeholder="Nuevo Nombre" 
                        value={nuevoNombreClan} 
                        onChange={(e) => setNuevoNombreClan(e.target.value)}
                        style={{ padding: '8px', borderRadius: '4px', border: 'none', flex: 1 }}
                      />
                      <button onClick={handleRenombrarClan} style={{ backgroundColor: '#4caf50', color: 'white', border: 'none', padding: '8px 15px', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>
                        Actualizar
                      </button>
                    </div>
                  </div>

                  <div style={{ flex: 1, minWidth: '250px' }}>
                    <p style={{ margin: '0 0 5px 0', color: '#aaa' }}>Reclutar Miembro (Personajes sin clan)</p>
                    <div style={{ display: 'flex', gap: '10px' }}>
                      <select 
                        value={nuevoMiembroId} 
                        onChange={(e) => setNuevoMiembroId(e.target.value)}
                        style={{ padding: '8px', borderRadius: '4px', border: '1px solid #555', flex: 1, backgroundColor: '#333', color: 'white' }}
                      >
                        {personajesSinClan.length === 0 ? (
                          <option value="">No hay personajes disponibles</option>
                        ) : (
                          personajesSinClan.map(pj => (
                            <option key={pj.id_personaje || pj.idPersonaje} value={pj.id_personaje || pj.idPersonaje}>
                              {pj.nombre} (Nivel {pj.nivel} - {pj.clase})
                            </option>
                          ))
                        )}
                      </select>
                      <button 
                        onClick={handleAnadirMiembro} 
                        disabled={!nuevoMiembroId}
                        style={{ 
                          backgroundColor: nuevoMiembroId ? '#2196f3' : '#555', 
                          color: 'white', border: 'none', padding: '8px 15px', borderRadius: '4px', 
                          cursor: nuevoMiembroId ? 'pointer' : 'not-allowed', fontWeight: 'bold' 
                        }}>
                        Reclutar
                      </button>
                    </div>
                  </div>

                </div>
              </div>
            )}

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '20px' }}>
            {miembros.map(pj => {
              const pjId = (pj.id_personaje || pj.idPersonaje || pj.nombre).toString();
              
              // El líder se determina verificando si el ID de este personaje coincide con el id_lider del clan
              const liderId = clanInfo ? (clanInfo.id_lider || clanInfo.idLider) : null;
              const isLider = liderId && (pjId === liderId.toString());
              
              const isActivo = pjId === activePersonajeId;

              return (
                <div key={pjId} style={{
                  border: isLider ? '2px solid #ffd700' : '1px solid #444',
                  backgroundColor: isActivo ? '#1e3320' : '#1a1a1a',
                  padding: '20px',
                  borderRadius: '8px',
                  transition: 'all 0.3s'
                }}>
                  <h3 style={{ margin: '0 0 10px 0', color: isLider ? '#ffd700' : '#61dafb', fontSize: '22px', display: 'flex', justifyContent: 'space-between' }}>
                    <span>
                      {pj.nombre} {isActivo && <span style={{fontSize: '12px', color: '#4caf50', marginLeft: '5px'}}>(Tú)</span>}
                    </span>
                    {isLider && <span title="Líder del Clan">👑</span>}
                  </h3>
                  
                  <div style={{ backgroundColor: '#242424', padding: '10px', borderRadius: '5px' }}>
                    <p style={{ margin: '5px 0', color: '#aaa' }}>
                      <strong>Facción:</strong> <span style={{ color: pj.faccion === 'Horda' ? '#f44336' : (pj.faccion === 'Alianza' ? '#2196f3' : '#aaa') }}>{pj.faccion || 'Desconocida'}</span>
                    </p>
                    <p style={{ margin: '5px 0', color: '#aaa' }}>
                      <strong>Clase:</strong> {pj.clase}
                    </p>
                    <p style={{ margin: '5px 0', color: '#aaa' }}>
                      <strong>DKP (Mérito):</strong> <span style={{ color: '#ff9800', fontWeight: 'bold' }}>{pj.puntos_merito || pj.puntosMerito || 0}</span>
                    </p>
                    <p style={{ margin: '5px 0', color: '#aaa' }}>
                      <strong>iLvl:</strong> <span style={{ color: '#4caf50' }}>{pj.item_level || pj.itemLevel || 0}</span>
                    </p>
                    <p style={{ margin: '5px 0', color: '#aaa', display: 'flex', alignItems: 'center', gap: '5px' }}>
                      <strong>Rol en Clan:</strong> 
                      {isLeader ? (
                        <select 
                          value={pj.rol_clan || pj.rolClan || 'Miembro'}
                          onChange={(e) => handleCambiarRol(pj, e.target.value)}
                          style={{ backgroundColor: '#333', color: isLider ? '#ffd700' : 'white', border: '1px solid #555', padding: '2px 5px', borderRadius: '3px', cursor: 'pointer', fontWeight: isLider ? 'bold' : 'normal' }}
                        >
                          {((pj.rol_clan || pj.rolClan) === 'Líder' || (pj.rol_clan || pj.rolClan) === 'LIDER') && (
                            <option value={pj.rol_clan || pj.rolClan} disabled hidden>{pj.rol_clan || pj.rolClan}</option>
                          )}
                          <option value="Raider">Raider</option>
                          <option value="Miembro">Miembro</option>
                        </select>
                      ) : (
                        <span style={{ color: isLider ? '#ffd700' : '#e91e63', fontWeight: isLider ? 'bold' : 'normal' }}>
                          {pj.rol_clan || pj.rolClan || 'Miembro'}
                        </span>
                      )}
                    </p>
                  </div>

                  {isLeader && !isLider && (
                    <div style={{ display: 'flex', gap: '10px', marginTop: '15px' }}>
                      <button onClick={() => handleAsignarLider(pjId)} style={{ flex: 1, backgroundColor: '#ff9800', color: 'white', border: 'none', padding: '8px', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold', fontSize: '12px' }}>
                        Hacer Líder
                      </button>
                      <button onClick={() => handleExpulsarMiembro(pjId)} style={{ flex: 1, backgroundColor: '#f44336', color: 'white', border: 'none', padding: '8px', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold', fontSize: '12px' }}>
                        Expulsar
                      </button>
                    </div>
                  )}

                </div>
              );
            })}
          </div>
          </>
        )}
      </div>
    </div>
  );
}

export default MiClan;
