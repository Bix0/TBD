import React, { useEffect, useState } from 'react';
// Cambiamos TileLayer por ImageOverlay
import { MapContainer, ImageOverlay, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import api from '../services/api'; 

// =======================================================
// --- DISEÑO PERSONALIZADO MMORPG (MARCADOR DE BOSS) ---
// =======================================================
const bossIcon = new L.Icon({
    iconUrl: 'https://cdn-icons-png.flaticon.com/512/3593/3593502.png', 
    iconSize: [40, 40],
    iconAnchor: [20, 40],
    popupAnchor: [0, -40]
});

const MapaRaids = () => {
    const [raids, setRaids] = useState([]);

    useEffect(() => {
        api.get('/api/raids/cercanas', {
            params: {
                // Ahora buscamos desde el centro del mapa 2D (500, 500)
                lon: 500,
                lat: 500,
                // Aumentamos la distancia para que barra todo nuestro mapa de 1000x1000
                distancia: 2000 
            }
        })
        .then(response => {
            setRaids(response.data || []);
        })
        .catch(error => {
            console.error("Error cargando el radar espacial:", error);
        });
    }, []);

    // Definimos el tamaño de nuestra imagen/plano
    const bounds = [[0, 0], [1000, 1000]];

    return (
        <MapContainer 
            // Configuración plana requerida para mapas de imágenes estáticas
            crs={L.CRS.Simple} 
            bounds={bounds}
            style={{ height: '700px', width: '100%', backgroundColor: '#000' }} 
            className="leaflet-container"
        >
            
            {/* --- IMAGEN DEL JUEGO (Debe estar en public/mapa-juego.jpg) --- */}
            <ImageOverlay
                url="/mapa-juego.jpg" 
                bounds={bounds}
            />
            
            {raids.map(raid => {
                const lat = raid.ubicacionBoss?.y || raid.ubicacionBoss?.coordinates?.[1] || 0;
                const lon = raid.ubicacionBoss?.x || raid.ubicacionBoss?.coordinates?.[0] || 0;

                if (lat === 0 && lon === 0) return null; 

                return (
                    <Marker key={raid.idRaid} position={[lat, lon]} icon={bossIcon}>
                        <Popup>
                            <div style={{ textAlign: 'center', minWidth: '120px' }}>
                                <strong style={{ color: '#aa3bff', fontSize: '16px' }}>💀 {raid.nombre}</strong>
                                <hr style={{ borderColor: '#444', margin: '5px 0' }} />
                                Poder Requerido: <b>{raid.itemLevelRequerido}</b><br />
                                Estado: {raid.estado}
                            </div>
                        </Popup>
                    </Marker>
                );
            })}
        </MapContainer>
    );
};

export default MapaRaids;