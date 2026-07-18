import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import api from '../services/api'; // Importamos el interceptor con el token JWT

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
        // Usamos nuestro interceptor 'api' que incluye el Authorization Bearer automáticamente
        api.get('/api/raids/cercanas', {
            params: {
                lon: -70.64827,
                lat: -33.45694,
                distancia: 10000
            }
        })
        .then(response => {
            // response.data ya contiene el JSON listo
            setRaids(response.data || []);
        })
        .catch(error => {
            console.error("Error cargando el radar espacial:", error);
            // Si el error es 403, el usuario no tiene permisos
        });
    }, []);

    return (
        <MapContainer center={[-33.45694, -70.64827]} zoom={11} className="leaflet-container">
            
            {/* --- TILELAYER OSCURO TIPO RPG --- */}
            <TileLayer
                attribution='&copy; <a href="https://carto.com/attributions">CARTO</a>'
                url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
            />
            
            {raids.map(raid => {
                // Adaptación a cómo Hibernate Spatial serializa los datos (Geometry Point)
                // A veces llega como {y, x} o {coordinates: [lon, lat]}
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