import { useState, useEffect } from 'react';
import axios from 'axios';
import Navbar from '../components/Navbar';

function PanelAdmin() {
  const rol = localStorage.getItem('rol');
  const token = localStorage.getItem('token');
  const configSeguridad = { headers: { Authorization: `Bearer ${token}` } };

  const [items, setItems] = useState([]);
  const [personajes, setPersonajes] = useState([]);
  const [raids, setRaids] = useState([]);
  const [auditoria, setAuditoria] = useState([]);

  const [nuevoItem, setNuevoItem] = useState({ nombre: '', item_lvl: 0, ganancia_dkp: 0 });
  const [raidData, setRaidData] = useState({ nombre: '', fecha: '', item_level_requerido: 0, cupos_tanque: 0, cupos_healer: 0, cupos_dps: 0 });
  
  const [godDkp, setGodDkp] = useState({ idPersonaje: '', cantidad: 0 });
  const [godLoot, setGodLoot] = useState({ idPersonaje: '', idItem: '', idRaid: '' });
  
  const [simulando, setSimulando] = useState(false);
  const [modalSimulacion, setModalSimulacion] = useState(null);
  const [itemRecompensa, setItemRecompensa] = useState('');
  const [mensajeSimulacion, setMensajeSimulacion] = useState('');
  const [tiempoRestante, setTiempoRestante] = useState(0);

  useEffect(() => { if (rol === 'Admin') cargarDatosAdmin(); }, [rol]);

  const cargarDatosAdmin = async () => {
    try {
      const resItems = await axios.get('http://localhost:8080/api/items', configSeguridad);
      const resPjs = await axios.get('http://localhost:8080/api/personajes', configSeguridad);
      const resRaids = await axios.get('http://localhost:8080/api/raids', configSeguridad);
      
      axios.get('http://localhost:8080/api/clanes/auditoria', configSeguridad)
        .then(res => setAuditoria(res.data))
        .catch(() => setAuditoria([]));

      setItems(resItems.data); setPersonajes(resPjs.data); setRaids(resRaids.data);
    } catch (error) { console.error(error); }
  };

  const handleGodDkp = (e) => {
    e.preventDefault();
    axios.put(`http://localhost:8080/api/personajes/${godDkp.idPersonaje}/merito?cantidad=${-godDkp.cantidad}`, null, configSeguridad)
      .then(() => { alert(`¡DKP inyectado al personaje!`); cargarDatosAdmin(); })
      .catch((err) => alert("Error del Servidor: " + (err.response?.data || err.message)));
  };

  const handleGodLoot = (e) => {
    e.preventDefault();
    if (!godLoot.idPersonaje || !godLoot.idItem || !godLoot.idRaid) return alert("Faltan datos por seleccionar.");
    
    // Inyecta directamente al SP con costo 0 (Modo Dios)
    axios.post(`http://localhost:8080/api/raids/distribuir-loot?idPersonaje=${godLoot.idPersonaje}&idItem=${godLoot.idItem}&idRaid=${godLoot.idRaid}&costoDkp=0`, null, configSeguridad)
      .then(() => { 
        alert("¡Botín de los Dioses entregado con éxito! Revisa el Inventario y el Historial de Botín."); 
        cargarDatosAdmin(); 
      })
      .catch((err) => {
        
        alert("Error en el Backend al entregar botín: " + (err.response?.data || err.message));
      });
  };

  const handleItemSubmit = (e) => {
    e.preventDefault();
    axios.post('http://localhost:8080/api/items', nuevoItem, configSeguridad).then(() => { alert("Ítem creado."); cargarDatosAdmin(); });
  };

  const handleRaidSubmit = (e) => {
    e.preventDefault();
    axios.post('http://localhost:8080/api/raids', { ...raidData, estado: 'Programada' }, configSeguridad).then(() => { alert("¡Raid Programada!"); cargarDatosAdmin(); });
  };

const ejecutarSimulacion = async () => {
    if (!itemRecompensa) return alert("Selecciona la recompensa primero.");
    setSimulando(true); setMensajeSimulacion("⚔️ ¡La batalla ha comenzado!"); setTiempoRestante(5);

    try {
      const resInscritos = await axios.get(`http://localhost:8080/api/raids/${modalSimulacion.id_raid}/inscripciones`, configSeguridad);
      const inscritos = resInscritos.data;

      if (inscritos.length === 0) {
        setSimulando(false); setTiempoRestante(0);
        return setMensajeSimulacion("❌ Nadie se inscribió. La Raid fracasó.");
      }

      // CANDADO DE SEGURIDAD PARA EVITAR DOBLE COBRO
      let finalizado = false; 
      const intervalo = setInterval(() => {
        setTiempoRestante(prev => {
          if (prev <= 1) { 
            clearInterval(intervalo); 
            if(!finalizado) {
              finalizado = true;
              finalizarBatalla(inscritos); 
            }
            return 0; 
          }
          return prev - 1;
        });
      }, 1000);
    } catch (error) { setSimulando(false); setTiempoRestante(0); }
  };

  const finalizarBatalla = async (inscritos) => {
    const pjsInscritos = inscritos.map(ins => personajes.find(p => p.id_personaje === ins[1])).filter(Boolean);
    pjsInscritos.sort((a, b) => b.puntos_merito - a.puntos_merito);
    const ganador = pjsInscritos[0]; 
    const itemGanado = items.find(i => i.id_item === parseInt(itemRecompensa));
    
    const costoFinal = itemGanado.ganancia_dkp !== undefined ? itemGanado.ganancia_dkp : itemGanado.gananciaDkp;

    setMensajeSimulacion(`🏆 ¡Jefe Muerto! Entregando objeto a ${ganador.nombre}...`);

    try {
      await axios.post(`http://localhost:8080/api/raids/distribuir-loot?idPersonaje=${ganador.id_personaje}&idItem=${itemGanado.id_item}&idRaid=${modalSimulacion.id_raid}&costoDkp=${costoFinal}`, null, configSeguridad);
      await axios.put(`http://localhost:8080/api/raids/${modalSimulacion.id_raid}/estado?estado=Completada`, null, configSeguridad);
      await axios.post('http://localhost:8080/api/ranking/refresh', null, configSeguridad);
      
      setMensajeSimulacion(`✅ Éxito: Se entregó el ítem a ${ganador.nombre} y se descontaron ${costoFinal} DKP.`);
      cargarDatosAdmin(); 
    } catch (err) { 
    
      setMensajeSimulacion("❌ Error de BD: " + (err.response?.data || err.message)); 
    }
    setSimulando(false);
  };

  if (rol !== 'Admin') return <div style={{ minHeight: '100vh', backgroundColor: '#121212' }}><h1 style={{ textAlign: 'center', marginTop: '100px', color: 'red' }}>Acceso Denegado</h1></div>;

  return (
    <div style={{ backgroundColor: '#121212', minHeight: '100vh', color: 'white', paddingBottom: '50px' }}>
      <Navbar />
      <div style={{ padding: '20px', maxWidth: '900px', margin: '0 auto' }}>
        
        {/* MODO DIOS */}
        <h2 style={{ textAlign: 'center', color: '#00e676', borderBottom: '2px solid #00e676', paddingBottom: '10px' }}>⚡ Herramientas Divinas (Pruebas)</h2>
        <div style={{ backgroundColor: '#1a1a1a', padding: '20px', borderRadius: '8px', marginBottom: '40px', border: '1px dashed #00e676' }}>
          
          <form onSubmit={handleGodDkp} style={{ display: 'flex', gap: '10px', marginBottom: '15px' }}>
            <select value={godDkp.idPersonaje} onChange={e => setGodDkp({...godDkp, idPersonaje: e.target.value})} required style={{ flex: 2, padding: '8px', backgroundColor: '#333', color: 'white' }}>
              <option value="">-- Regalar DKP a Personaje --</option>
              {personajes.map(p => <option key={p.id_personaje} value={p.id_personaje}>{p.nombre} (DKP Actual: {p.puntos_merito})</option>)}
            </select>
            <input type="number" placeholder="Cantidad de DKP" value={godDkp.cantidad} onChange={e => setGodDkp({...godDkp, cantidad: e.target.value})} required style={{ flex: 1, padding: '8px', backgroundColor: '#333', color: 'white' }} />
            <button type="submit" style={{ flex: 1, backgroundColor: '#00e676', color: 'black', fontWeight: 'bold', border: 'none', cursor: 'pointer' }}>Inyectar DKP</button>
          </form>

          {/* BOTÓN MODO DIOS BOTÍN */}
          <form onSubmit={handleGodLoot} style={{ display: 'flex', gap: '10px' }}>
            <select value={godLoot.idPersonaje} onChange={e => setGodLoot({...godLoot, idPersonaje: e.target.value})} required style={{ flex: 1, padding: '8px', backgroundColor: '#333', color: 'white' }}>
              <option value="">-- Personaje --</option>
              {personajes.map(p => <option key={p.id_personaje} value={p.id_personaje}>{p.nombre}</option>)}
            </select>
            <select value={godLoot.idItem} onChange={e => setGodLoot({...godLoot, idItem: e.target.value})} required style={{ flex: 1, padding: '8px', backgroundColor: '#333', color: 'white' }}>
              <option value="">-- Ítem a Regalar --</option>
              {items.map(i => <option key={i.id_item} value={i.id_item}>{i.nombre}</option>)}
            </select>
            <select value={godLoot.idRaid} onChange={e => setGodLoot({...godLoot, idRaid: e.target.value})} required style={{ flex: 1, padding: '8px', backgroundColor: '#333', color: 'white' }}>
              <option value="">-- ¿En qué Raid? --</option>
              {raids.map(r => <option key={r.id_raid} value={r.id_raid}>{r.nombre}</option>)}
            </select>
            <button type="submit" style={{ padding: '8px', backgroundColor: '#ba68c8', color: 'white', fontWeight: 'bold', border: 'none', cursor: 'pointer' }}>Forzar Historial Botín</button>
          </form>

        </div>

        {/* CATÁLOGO DE ÍTEMS Y PROGRAMAR RAID */}
        <h2 style={{ textAlign: 'center', color: '#ba68c8', borderBottom: '2px solid #ba68c8', paddingBottom: '10px' }}>⚔️ Catálogo de Armería</h2>
        <div style={{ backgroundColor: '#1a1a1a', padding: '20px', borderRadius: '8px', marginBottom: '40px' }}>
          <form onSubmit={handleItemSubmit} style={{ display: 'flex', gap: '15px', alignItems: 'flex-end', marginBottom: '20px' }}>
            <div style={{ flex: 2 }}><label style={{ color: '#aaa', fontSize: '12px' }}>Nombre del Ítem</label><input type="text" value={nuevoItem.nombre} onChange={e => setNuevoItem({...nuevoItem, nombre: e.target.value})} required style={{ width: '100%', padding: '8px' }} /></div>
            <div style={{ flex: 1 }}><label style={{ color: '#aaa', fontSize: '12px' }}>Poder (iLvl)</label><input type="number" value={nuevoItem.item_lvl} onChange={e => setNuevoItem({...nuevoItem, item_lvl: e.target.value})} required style={{ width: '100%', padding: '8px' }} /></div>
            <div style={{ flex: 1 }}><label style={{ color: '#aaa', fontSize: '12px' }}>Costo DKP</label><input type="number" value={nuevoItem.ganancia_dkp} onChange={e => setNuevoItem({...nuevoItem, ganancia_dkp: e.target.value})} required style={{ width: '100%', padding: '8px' }} /></div>
            <button type="submit" style={{ padding: '9px 15px', backgroundColor: '#ba68c8', color: 'white', border: 'none', cursor: 'pointer', fontWeight: 'bold' }}>Crear</button>
          </form>
          <div style={{ maxHeight: '150px', overflowY: 'auto' }}>
            {items.map(i => (
              <div key={i.id_item} style={{ padding: '10px', borderBottom: '1px solid #333' }}>
                <strong>{i.nombre}</strong> <span style={{ color: '#aaa' }}>| Otorga: <strong style={{ color: '#61dafb' }}>+{i.item_lvl} Poder</strong> | Cuesta: <strong style={{ color: '#ff9800' }}>-{i.ganancia_dkp} DKP</strong></span>
              </div>
            ))}
          </div>
        </div>

        <h2 style={{ textAlign: 'center', color: '#f44336', borderBottom: '2px solid #f44336', paddingBottom: '10px' }}>🏰 Programar Raid</h2>
        <form onSubmit={handleRaidSubmit} style={{ backgroundColor: '#1a1a1a', padding: '20px', borderRadius: '8px', marginBottom: '40px' }}>
          <div style={{ display: 'flex', gap: '15px', marginBottom: '15px' }}>
            <div style={{ flex: 2 }}><label style={{ color: '#aaa', fontSize: '12px' }}>Nombre</label><input type="text" value={raidData.nombre} onChange={e => setRaidData({...raidData, nombre: e.target.value})} required style={{ width: '100%', padding: '8px' }} /></div>
            <div style={{ flex: 1 }}><label style={{ color: '#aaa', fontSize: '12px' }}>Fecha</label><input type="datetime-local" value={raidData.fecha} onChange={e => setRaidData({...raidData, fecha: e.target.value})} required style={{ width: '100%', padding: '8px' }} /></div>
            <div style={{ flex: 1 }}><label style={{ color: '#aaa', fontSize: '12px' }}>Poder Req.</label><input type="number" value={raidData.item_level_requerido} onChange={e => setRaidData({...raidData, item_level_requerido: e.target.value})} required style={{ width: '100%', padding: '8px' }} /></div>
          </div>
          <div style={{ display: 'flex', gap: '15px', marginBottom: '20px' }}>
             <div style={{ flex: 1 }}><label style={{ color: '#aaa', fontSize: '12px' }}>Tanques</label><input type="number" value={raidData.cupos_tanque} onChange={e => setRaidData({...raidData, cupos_tanque: e.target.value})} required style={{ width: '100%', padding: '8px' }} /></div>
             <div style={{ flex: 1 }}><label style={{ color: '#aaa', fontSize: '12px' }}>Healers</label><input type="number" value={raidData.cupos_healer} onChange={e => setRaidData({...raidData, cupos_healer: e.target.value})} required style={{ width: '100%', padding: '8px' }} /></div>
             <div style={{ flex: 1 }}><label style={{ color: '#aaa', fontSize: '12px' }}>DPS</label><input type="number" value={raidData.cupos_dps} onChange={e => setRaidData({...raidData, cupos_dps: e.target.value})} required style={{ width: '100%', padding: '8px' }} /></div>
          </div>
          <button type="submit" style={{ width: '100%', padding: '10px', backgroundColor: '#f44336', color: 'white', fontWeight: 'bold', border: 'none', cursor: 'pointer' }}>Publicar Raid</button>
        </form>

        {/* SIMULADOR DE RAID */}
        <h2 style={{ textAlign: 'center', color: '#ff9800', borderBottom: '2px solid #ff9800', paddingBottom: '10px' }}>💎 Simular Batalla</h2>
        <div style={{ backgroundColor: '#1a1a1a', padding: '20px', borderRadius: '8px', marginBottom: '40px' }}>
          <div style={{ display: 'grid', gap: '15px' }}>
            {raids.filter(r => r.estado === 'Programada').map(r => (
              <div key={r.id_raid} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '15px', backgroundColor: '#242424', border: '1px solid #444', borderRadius: '6px' }}>
                <strong style={{ color: '#61dafb', fontSize: '18px' }}>{r.nombre}</strong>
                <button onClick={() => setModalSimulacion(r)} style={{ padding: '8px 15px', backgroundColor: '#ff9800', color: 'black', fontWeight: 'bold', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Simular Evento</button>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* MODAL DEL SIMULADOR */}
      {modalSimulacion && (
        <div style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0,0,0,0.85)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 }}>
          <div style={{ backgroundColor: '#1e1e1e', padding: '30px', borderRadius: '8px', width: '450px', border: '1px solid #ff9800', textAlign: 'center' }}>
            <h2 style={{ color: '#ff9800', marginTop: 0 }}>Raid: {modalSimulacion.nombre}</h2>
            <select value={itemRecompensa} onChange={(e) => setItemRecompensa(e.target.value)} disabled={simulando} style={{ width: '100%', padding: '10px', borderRadius: '4px', backgroundColor: '#333', color: 'white', border: 'none', marginBottom: '20px' }}>
              <option value="">-- Elige la recompensa --</option>
              {items.map(i => <option key={i.id_item} value={i.id_item}>{i.nombre} (Cuesta: {i.ganancia_dkp} DKP)</option>)}
            </select>
            {simulando && <div style={{ fontSize: '40px', fontWeight: 'bold', color: '#f44336', margin: '20px 0' }}>⏳ {tiempoRestante}s</div>}
            {mensajeSimulacion && <div style={{ backgroundColor: '#333', padding: '15px', borderRadius: '6px', marginBottom: '20px', color: '#61dafb' }}>{mensajeSimulacion}</div>}
            <div style={{ display: 'flex', gap: '10px', justifyContent: 'center' }}>
              {!simulando && <button onClick={() => { setModalSimulacion(null); setMensajeSimulacion(''); }} style={{ padding: '10px 20px', backgroundColor: '#555', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Cerrar</button>}
              <button onClick={ejecutarSimulacion} disabled={simulando} style={{ padding: '10px 20px', backgroundColor: simulando ? '#444' : '#ff9800', color: simulando ? '#888' : 'black', border: 'none', borderRadius: '4px', cursor: simulando ? 'not-allowed' : 'pointer', fontWeight: 'bold' }}>
                {simulando ? 'Batallando...' : 'Iniciar Simulación'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default PanelAdmin;