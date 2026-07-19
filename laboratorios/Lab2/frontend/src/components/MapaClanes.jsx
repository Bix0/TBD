import React, { useEffect, useState } from "react";
import {
  MapContainer,
  ImageOverlay,
  CircleMarker,
  Popup,
  Tooltip,
} from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import api from "../services/api";

const MapaClanes = () => {
  const [clanesCalor, setClanesCalor] = useState([]);

  useEffect(() => {
    api
      .get("/api/clanes/mapa-calor")
      .then((res) => setClanesCalor(res.data))
      .catch((err) => console.error("Error cargando mapa de calor:", err));
  }, []);

  const calcularRadio = (dkp) => {
    const base = 10;
    const extra = Math.min(dkp / 50, 40);
    return base + extra;
  };

  // Definimos los límites del mapa (Plano de 1000x1000)
  // El formato siempre es [Y, X]
  const bounds = [
    [0, 0],
    [1000, 1000],
  ];

  return (
    <MapContainer
      crs={L.CRS.Simple}
      bounds={bounds}
      maxBounds={bounds}
      maxBoundsViscosity={1.0}
      style={{ height: "700px", width: "100%", backgroundColor: "#000" }}
      className="leaflet-container"
    >
      {/* Aquí cargamos tu imagen desde la carpeta public */}
      <ImageOverlay url="/mapa-juego.jpg" bounds={bounds} />

      {clanesCalor.map((clan, index) => {
        const [id, nombre, lat, lon, dkpTotal] = clan;

        return (
          <CircleMarker
            key={index}
            // lat y lon ahora representan el eje Y y X en el plano de 0 a 1000
            center={[lat, lon]}
            pathOptions={{
              color: "#ff4b4b",
              fillColor: "#ff0000",
              fillOpacity: 0.6,
            }}
            radius={calcularRadio(dkpTotal)}
          >
            <Tooltip
              direction="top"
              offset={[0, -10]}
              opacity={0.9}
              permanent={false}
            >
              <span style={{ fontWeight: "bold", color: "#aa3bff" }}>
                {nombre}
              </span>
            </Tooltip>
            <Popup>
              <div style={{ textAlign: "center" }}>
                <strong style={{ color: "#aa3bff", fontSize: "16px" }}>
                  🏰 {nombre}
                </strong>
                <hr style={{ borderColor: "#444", margin: "5px 0" }} />
                Poder Total del Clan (DKP):{" "}
                <b style={{ color: "#ff4b4b" }}>{dkpTotal}</b>
              </div>
            </Popup>
          </CircleMarker>
        );
      })}
    </MapContainer>
  );
};

export default MapaClanes;
