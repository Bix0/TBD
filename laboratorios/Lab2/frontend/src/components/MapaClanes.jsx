import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, CircleMarker, Popup, Tooltip } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import api from '../services/api'; // <-- Importamos tu instancia de Axios

const MapaClanes = () => {
    const [clanesCalor, setClanesCalor] = useState([]);

    useEffect(() => {
        // Usamos la instancia 'api' que ya inyecta el token y maneja los errores
        api.get('/api/clanes/mapa-calor')
        .then(res => setClanesCalor(res.data)) // Axios ya parsea el JSON y lo guarda en res.data
        .catch(err => console.error("Error cargando mapa de calor:", err));
    }, []);

    // Función para calcular el radio del círculo según el DKP
    const calcularRadio = (dkp) => {
        const base = 10;
        const extra = Math.min(dkp / 50, 40); // Límite máximo de crecimiento
        return base + extra;
    };

    return (
        <MapContainer center={[-33.45694, -70.64827]} zoom={11} className="leaflet-container">
            {/* Tema oscuro para mantener el estilo MMORPG */}
            <TileLayer
                attribution='&copy; <a href="https://carto.com/attributions">CARTO</a>'
                url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
            />
            
            {clanesCalor.map((clan, index) => {
                // El backend nos devuelve un Object[]: [id_clan, nombre, lat, lon, dkp_total]
                const [id, nombre, lat, lon, dkpTotal] = clan;

                return (
                    <CircleMarker 
                        key={index} 
                        center={[lat, lon]} 
                        pathOptions={{ 
                            color: '#ff4b4b', 
                            fillColor: '#ff0000', 
                            fillOpacity: 0.6 
                        }} 
                        radius={calcularRadio(dkpTotal)}
                    >
                        <Tooltip direction="top" offset={[0, -10]} opacity={0.9} permanent={false}>
                            <span style={{ fontWeight: 'bold', color: '#aa3bff' }}>{nombre}</span>
                        </Tooltip>
                        <Popup>
                            <div style={{ textAlign: 'center' }}>
                                <strong style={{ color: '#aa3bff', fontSize: '16px' }}>🏰 {nombre}</strong>
                                <hr style={{ borderColor: '#444', margin: '5px 0' }} />
                                Poder Total del Clan (DKP): <b style={{color: '#ff4b4b'}}>{dkpTotal}</b>
                            </div>
                        </Popup>
                    </CircleMarker>
                );
            })}
        </MapContainer>
    );
};

export default MapaClanes;