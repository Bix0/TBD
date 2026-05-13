import { useState, useEffect } from 'react';
import axios from 'axios';
import Navbar from '../components/Navbar';

function Personajes() {
    const [personajes, setPersonajes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [formData, setFormData] = useState({ nombre: '', clase: 'Guerrero', faccion: 'Alianza', rol_clan: 'DPS' });

    const userId = localStorage.getItem('userId');
    const token = localStorage.getItem('token');
    const configSeguridad = { headers: { Authorization: `Bearer ${token}` } };
    const [activoId, setActivoId] = useState(localStorage.getItem('activePersonajeId'));

    const cargarPersonajes = () => {
        setLoading(true);
        axios.get(`http://localhost:8080/api/personajes/jugador/${userId}/todos`, configSeguridad)
            .then(res => setPersonajes(res.data))
            .catch(() => setPersonajes([]))
            .finally(() => setLoading(false));
    };

    useEffect(() => { cargarPersonajes(); }, []);

    const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

    const crearPersonaje = (e) => {
        e.preventDefault();
        const nuevoPersonaje = {
            id_jugador: parseInt(userId), id_clan: 1, nombre: formData.nombre,
            clase: formData.clase, faccion: formData.faccion, rol_clan: formData.rol_clan,
            nivel: 1, item_level: 10, puntos_merito: 0
        };
        axios.post('http://localhost:8080/api/personajes', nuevoPersonaje, configSeguridad)
            .then(() => { alert("Personaje forjado con éxito."); cargarPersonajes(); })
            .catch(() => alert("Error al crear. Es probable que el nombre ya esté en uso."));
    };

    const subirNivel = (personaje) => {
        const personajeActualizado = { 
            ...personaje, 
            nivel: personaje.nivel + 1,
            item_level: personaje.item_level + 1 
        };
        axios.put(`http://localhost:8080/api/personajes/${personaje.id_personaje}`, personajeActualizado, configSeguridad)
            .then(() => cargarPersonajes())
            .catch(() => alert("Error al actualizar personaje."));
    };

    const bajarNivel = (personaje) => {
        if (personaje.nivel <= 1) return alert("Un personaje no puede tener un nivel menor a 1.");
        const personajeActualizado = { 
            ...personaje, 
            nivel: personaje.nivel - 1,
            item_level: personaje.item_level - 1 
        };
        axios.put(`http://localhost:8080/api/personajes/${personaje.id_personaje}`, personajeActualizado, configSeguridad)
            .then(() => cargarPersonajes())
            .catch(() => alert("Error al actualizar personaje."));
    };

    const borrarPersonaje = (id) => {
        if(window.confirm("¿Estás seguro? Se borrará su inventario y progreso para siempre.")){
            axios.delete(`http://localhost:8080/api/personajes/${id}`, configSeguridad)
                .then(() => {
                    if (activoId == id) { localStorage.removeItem('activePersonajeId'); setActivoId(null); }
                    cargarPersonajes();
                }).catch(() => alert("Error al borrar."));
        }
    };

    const seleccionarActivo = (id) => {
        localStorage.setItem('activePersonajeId', id);
        setActivoId(id.toString());
    };

    return (
        <div style={{ backgroundColor: '#121212', minHeight: '100vh', color: 'white', paddingBottom: '50px' }}>
            <Navbar />
            <div style={{ padding: '20px', maxWidth: '900px', margin: '20px auto' }}>
                <h1 style={{ textAlign: 'center', color: '#61dafb', marginBottom: '30px' }}>Mis Personajes</h1>

                {loading ? <p style={{textAlign:'center', color: '#aaa'}}>Cargando héroes...</p> : (
                    <div style={{ display: 'grid', gap: '20px', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))' }}>
                        {personajes.length === 0 && <p style={{ gridColumn: '1/-1', textAlign: 'center', color: '#888' }}>Aún no has creado ningún personaje.</p>}
                        
                        {personajes.map(p => (
                            <div key={p.id_personaje} style={{ backgroundColor: '#1a1a1a', padding: '20px', borderRadius: '8px', border: activoId == p.id_personaje ? '2px solid #ff9800' : '1px solid #444', textAlign: 'center' }}>
                                {activoId == p.id_personaje && <div style={{ color: '#ff9800', fontWeight: 'bold', marginBottom: '10px' }}>⭐ ACTIVO</div>}
                                <h2 style={{ color: '#81c784', margin: '0 0 5px 0' }}>{p.nombre}</h2>
                                <p style={{ color: '#aaa', margin: '0 0 5px 0' }}>{p.clase} | {p.faccion}</p>
                                <p style={{ color: '#61dafb', margin: '0 0 15px 0' }}>Nivel: {p.nivel} | Rol: {p.rol_clan}</p>
                                
                                <div style={{ display: 'flex', gap: '5px', justifyContent: 'center', flexWrap: 'wrap' }}>
                                    <button onClick={() => seleccionarActivo(p.id_personaje)} style={{ flex: '1 1 45%', padding: '8px', backgroundColor: '#4caf50', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>Usar</button>
                                    <button onClick={() => borrarPersonaje(p.id_personaje)} style={{ flex: '1 1 45%', padding: '8px', backgroundColor: '#f44336', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Borrar</button>
                                    <button onClick={() => subirNivel(p)} style={{ flex: '1 1 45%', padding: '8px', backgroundColor: '#2196f3', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>+1 Nivel</button>
                                    <button onClick={() => bajarNivel(p)} style={{ flex: '1 1 45%', padding: '8px', backgroundColor: '#ff9800', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>-1 Nivel</button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}

                <form onSubmit={crearPersonaje} style={{ backgroundColor: '#1a1a1a', padding: '30px', borderRadius: '8px', border: '1px solid #333', marginTop: '40px' }}>
                    <h3 style={{ color: '#aaa', marginBottom: '20px', borderBottom: '1px solid #333', paddingBottom: '10px' }}>Crear Nuevo Personaje</h3>
                    
                    <div style={{ display: 'flex', gap: '15px', flexWrap: 'wrap' }}>
                        
                        <div style={{ flex: '1 1 200px' }}>
                            <label style={{ display: 'block', color: '#aaa', fontSize: '13px', marginBottom: '5px' }}>Nombre del Personaje</label>
                            <input type="text" name="nombre" placeholder="Ej: Arthas" value={formData.nombre} onChange={handleChange} required style={{ width: '100%', padding: '10px', borderRadius: '4px', border: 'none', backgroundColor: '#333', color: 'white', boxSizing: 'border-box' }} />
                        </div>

                        <div style={{ flex: '1 1 150px' }}>
                            <label style={{ display: 'block', color: '#aaa', fontSize: '13px', marginBottom: '5px' }}>Clase / Tipo</label>
                            <select name="clase" value={formData.clase} onChange={handleChange} style={{ width: '100%', padding: '10px', borderRadius: '4px', border: 'none', backgroundColor: '#333', color: 'white', boxSizing: 'border-box' }}>
                                <option value="Guerrero"> Guerrero</option>
                                <option value="Mago"> Mago</option>
                                <option value="Ranger"> Ranger</option>
                            </select>
                        </div>

                        <div style={{ flex: '1 1 150px' }}>
                            <label style={{ display: 'block', color: '#aaa', fontSize: '13px', marginBottom: '5px' }}>Facción (Bando)</label>
                            <select name="faccion" value={formData.faccion} onChange={handleChange} style={{ width: '100%', padding: '10px', borderRadius: '4px', border: 'none', backgroundColor: '#333', color: 'white', boxSizing: 'border-box' }}>
                                <option value="Alianza">🛡️ Alianza</option>
                                <option value="Horda">🪓 Horda</option>
                            </select>
                        </div>

                        <div style={{ flex: '1 1 150px' }}>
                            <label style={{ display: 'block', color: '#aaa', fontSize: '13px', marginBottom: '5px' }}>Rol en Combate</label>
                            <select name="rol_clan" value={formData.rol_clan} onChange={handleChange} style={{ width: '100%', padding: '10px', borderRadius: '4px', border: 'none', backgroundColor: '#333', color: 'white', boxSizing: 'border-box' }}>
                                <option value="DPS"> DPS</option>
                                <option value="Tanque"> Tanque</option>
                                <option value="Healer"> Healer</option>
                            </select>
                        </div>

                    </div>
                    <button type="submit" style={{ width: '100%', marginTop: '25px', padding: '12px', backgroundColor: '#61dafb', color: '#000', border: 'none', borderRadius: '4px', fontWeight: 'bold', cursor: 'pointer', fontSize: '15px' }}>
                        Forjar Personaje
                    </button>
                </form>
            </div>
        </div>
    );
}

export default Personajes;