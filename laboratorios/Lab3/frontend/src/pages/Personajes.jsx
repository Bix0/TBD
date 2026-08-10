import { useState, useEffect } from 'react';
import api from '../services/api'; // Importamos tu nuevo interceptor limpio
import Navbar from '../components/Navbar';

function Personajes() {
    const [personajes, setPersonajes] = useState([]);
    const [loading, setLoading] = useState(true);

    // Cambiamos 'rol_clan' a 'rolClan' para coincidir con tu backend
    const [formData, setFormData] = useState({ nombre: '', clase: 'Guerrero', faccion: 'Alianza', rolClan: 'DPS' });

    const userId = localStorage.getItem('userId');
    const [activoId, setActivoId] = useState(localStorage.getItem('activePersonajeId'));

    const cargarPersonajes = () => {
        setLoading(true);
        // Usamos 'api' en lugar de 'axios', ya no necesitamos configSeguridad ni la URL completa
        api.get(`/api/personajes/jugador/${userId}/todos`)
            .then(res => setPersonajes(res.data))
            .catch(() => setPersonajes([]))
            .finally(() => setLoading(false));
    };

    useEffect(() => { cargarPersonajes(); }, []);

    const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

    const crearPersonaje = (e) => {
        e.preventDefault();

        // CORRECCIÓN CLAVE: Nombres exactos a tu Personaje.java (camelCase)
        const nuevoPersonaje = {
            // Nota: Spring Boot suele esperar objetos anidados para las relaciones @ManyToOne
            jugador: { idJugador: parseInt(userId) },
            clan: { idClan: formData.faccion === 'Horda' ? 2 : 1 }, // Clan 1 (Alianza) o Clan 2 (Horda)
            nombre: formData.nombre,
            clase: formData.clase,
            faccion: formData.faccion,
            rolClan: formData.rolClan,
            nivel: 1,
            itemLevel: 10,
            puntosMerito: 0
        };

        api.post('/api/personajes', nuevoPersonaje)
            .then((res) => {
                alert("Personaje forjado con éxito.");
                cargarPersonajes();
                // Selecciona automáticamente el personaje si el backend devuelve el ID
                if (res.data && res.data.idPersonaje) {
                    seleccionarActivo(res.data.idPersonaje);
                }
            })
            .catch((err) => {
                console.error("Error al crear:", err.response?.data);
                alert("Error al crear. Verifica la consola para más detalles.");
            });
    };

    const subirNivel = (personaje) => {
        const personajeActualizado = {
            ...personaje,
            nivel: personaje.nivel + 1,
            itemLevel: personaje.itemLevel + 1 // camelCase
        };
        api.put(`/api/personajes/${personaje.idPersonaje}`, personajeActualizado)
            .then(() => cargarPersonajes())
            .catch(() => alert("Error al actualizar personaje."));
    };

    const bajarNivel = (personaje) => {
        if (personaje.nivel <= 1) return alert("Un personaje no puede tener un nivel menor a 1.");
        const personajeActualizado = {
            ...personaje,
            nivel: personaje.nivel - 1,
            itemLevel: personaje.itemLevel - 1 // camelCase
        };
        api.put(`/api/personajes/${personaje.idPersonaje}`, personajeActualizado)
            .then(() => cargarPersonajes())
            .catch(() => alert("Error al actualizar personaje."));
    };

    const borrarPersonaje = (id) => {
        if (window.confirm("¿Estás seguro? Se borrará su inventario y progreso para siempre.")) {
            api.delete(`/api/personajes/${id}`)
                .then(() => {
                    if (activoId == id) {
                        localStorage.removeItem('activePersonajeId');
                        setActivoId(null);
                    }
                    cargarPersonajes();
                }).catch(() => alert("Error al borrar."));
        }
    };

    const salirDelClan = (personaje) => {
        if (!personaje.clan) return alert("El personaje no pertenece a ningún clan.");
        const idClan = personaje.clan.idClan || personaje.clan.id_clan || 1;
        const pjId = personaje.idPersonaje || personaje.id_personaje;
        if (window.confirm(`¿Seguro que deseas que ${personaje.nombre} salga del clan ${personaje.clan.nombre || ''}?`)) {
            api.post(`/api/clanes/salir/${idClan}`, pjId, { headers: { 'Content-Type': 'application/json' } })
                .then(() => {
                    alert(`${personaje.nombre} ha salido del clan.`);
                    cargarPersonajes();
                })
                .catch((err) => {
                    console.error("Error saliendo del clan:", err);
                    alert("Error al salir del clan.");
                });
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

                {loading ? <p style={{ textAlign: 'center', color: '#aaa' }}>Cargando héroes...</p> : (
                    <div style={{ display: 'grid', gap: '20px', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))' }}>
                        {personajes.length === 0 && <p style={{ gridColumn: '1/-1', textAlign: 'center', color: '#888' }}>Aún no has creado ningún personaje.</p>}

                        {personajes.map(p => (
                            /* Cambiamos id_personaje por idPersonaje en todo el mapeo */
                            <div key={p.idPersonaje} style={{ backgroundColor: '#1a1a1a', padding: '20px', borderRadius: '8px', border: activoId == p.idPersonaje ? '2px solid #ff9800' : '1px solid #444', textAlign: 'center' }}>
                                {activoId == p.idPersonaje && <div style={{ color: '#ff9800', fontWeight: 'bold', marginBottom: '10px' }}>⭐ ACTIVO</div>}
                                <h2 style={{ color: '#81c784', margin: '0 0 5px 0' }}>{p.nombre}</h2>
                                <p style={{ color: '#aaa', margin: '0 0 5px 0' }}>{p.clase} | {p.faccion}</p>
                                <p style={{ color: '#ab47bc', margin: '0 0 5px 0', fontSize: '13px' }}>🏰 Clan: <b>{p.clan ? p.clan.nombre : "Sin Clan"}</b></p>
                                <p style={{ color: '#61dafb', margin: '0 0 5px 0' }}>Nivel: {p.nivel} | Poder de Combate: <b>{p.itemLevel ?? p.item_level ?? 0} iLvl</b></p>
                                <p style={{ color: '#ffb74d', margin: '0 0 15px 0', fontSize: '13px' }}>⚔️ Rol: <b>{p.rolClan || p.rol_clan || p.rol || "Sin Rol"}</b></p>

                                <div style={{ display: 'flex', gap: '5px', justifyContent: 'center', flexWrap: 'wrap' }}>
                                    <button onClick={() => seleccionarActivo(p.idPersonaje)} style={{ flex: '1 1 45%', padding: '8px', backgroundColor: '#4caf50', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>Usar</button>
                                    <button onClick={() => borrarPersonaje(p.idPersonaje)} style={{ flex: '1 1 45%', padding: '8px', backgroundColor: '#f44336', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Borrar</button>
                                    <button onClick={() => subirNivel(p)} style={{ flex: '1 1 45%', padding: '8px', backgroundColor: '#2196f3', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>+1 Nivel</button>
                                    <button onClick={() => bajarNivel(p)} style={{ flex: '1 1 45%', padding: '8px', backgroundColor: '#ff9800', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>-1 Nivel</button>
                                    {p.clan && (
                                        <button onClick={() => salirDelClan(p)} style={{ flex: '1 1 100%', padding: '8px', backgroundColor: '#e91e63', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold', marginTop: '5px' }}>
                                            🚪 Salir del Clan
                                        </button>
                                    )}
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
                            <select name="rolClan" value={formData.rolClan} onChange={handleChange} style={{ width: '100%', padding: '10px', borderRadius: '4px', border: 'none', backgroundColor: '#333', color: 'white', boxSizing: 'border-box' }}>
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