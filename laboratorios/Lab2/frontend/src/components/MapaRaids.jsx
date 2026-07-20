import React, { useEffect, useState } from "react";
import { MapContainer, ImageOverlay, Marker, Popup, useMap } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import api from "../services/api";

const bossIcon = new L.Icon({
  iconUrl: "/boss_icon.png",
  iconSize: [40, 40],
  iconAnchor: [20, 40],
  popupAnchor: [0, -40],
});

const playerIcon = new L.Icon({
  iconUrl: "/player_icon.png",
  iconSize: [40, 40],
  iconAnchor: [20, 40],
  popupAnchor: [0, -40],
});

function RaidMapController({ onCenterChange }) {
  const map = useMap();

  useEffect(() => {
    const updateCenter = () => {
      const center = map.getCenter();
      onCenterChange({ lat: center.lat, lng: center.lng });
    };

    updateCenter();
    map.on("move", updateCenter);
    map.on("zoom", updateCenter);

    return () => {
      map.off("move", updateCenter);
      map.off("zoom", updateCenter);
    };
  }, [map, onCenterChange]);

  return null;
}

const MapaRaids = () => {
  const [raids, setRaids] = useState([]);
  const [personajes, setPersonajes] = useState([]);
  const [selectedPersonaje, setSelectedPersonaje] = useState("");
  const [mapCenter, setMapCenter] = useState({ lat: 500, lng: 500 });
  const userId = localStorage.getItem("userId");
  const activeId = localStorage.getItem("activePersonajeId");

  useEffect(() => {
    const personajeId = activeId || selectedPersonaje;

    api
      .get("/api/raids/cercanas", {
        params: {
          idPersonaje: personajeId || undefined,
          lon: mapCenter.lng,
          lat: mapCenter.lat,
          distancia: 500, // Reducido a 500 para que el radar no abarque todo el mapa
        },
      })
      .then((response) => setRaids(response.data || []))
      .catch((error) => console.error("Error cargando raids:", error));
  }, [activeId, mapCenter.lat, mapCenter.lng, selectedPersonaje]);

  useEffect(() => {
    if (userId) {
      api
        .get(`/api/personajes/jugador/${userId}/todos`)
        .then((res) => {
          setPersonajes(res.data || []);
          if (res.data?.length > 0 && !selectedPersonaje) {
            setSelectedPersonaje(res.data[0].idPersonaje);
          }
        })
        .catch((err) => console.error("Error cargando personajes:", err));
    }
  }, [userId]);

  const inscribir = async (idRaid) => {
    const personajeId = activeId || selectedPersonaje;
    if (!personajeId)
      return alert(
        "No tienes un personaje seleccionado. Ve a 'Mis Personajes' primero.",
      );
    try {
      const res = await api.post(
        `/api/raids/${idRaid}/inscribir?idPersonaje=${personajeId}`,
      );
      alert(res.data);
    } catch (e) {
      alert(e.response?.data || "Error al inscribirse");
    }
  };

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
      <RaidMapController onCenterChange={setMapCenter} />
      <ImageOverlay url="/mapa-juego.jpg" bounds={bounds} />

      {/* Renderizar jugador activo */}
      {(() => {
        const activePersonaje = personajes.find(p => (p.idPersonaje || p.id_personaje) == (activeId || selectedPersonaje));
        if (activePersonaje && activePersonaje.latitud != null && activePersonaje.longitud != null) {
          return (
            <Marker position={[activePersonaje.latitud, activePersonaje.longitud]} icon={playerIcon}>
              <Popup>
                <div style={{ textAlign: "center" }}>
                  <strong style={{ color: "#61dafb", fontSize: "16px" }}>🧑 {activePersonaje.nombre} (Tú)</strong>
                  <br />
                  Nivel: {activePersonaje.nivel} | Poder: {activePersonaje.itemLevel || activePersonaje.item_level}
                </div>
              </Popup>
            </Marker>
          );
        }
        return null;
      })()}

      {raids.map((raid) => {
        const lat =
          raid.latitud ||
          raid.ubicacionBoss?.y ||
          raid.ubicacionBoss?.coordinates?.[1] ||
          0;
        const lon =
          raid.longitud ||
          raid.ubicacionBoss?.x ||
          raid.ubicacionBoss?.coordinates?.[0] ||
          0;

        if (lat === 0 && lon === 0) return null;

        return (
          <Marker key={raid.idRaid} position={[lat, lon]} icon={bossIcon}>
            <Popup>
              <div style={{ textAlign: "center", minWidth: "180px" }}>
                <strong style={{ color: "#aa3bff", fontSize: "16px" }}>
                  💀 {raid.nombre}
                </strong>
                <hr style={{ borderColor: "#444", margin: "5px 0" }} />
                Poder Requerido: <b>{raid.itemLevelRequerido}</b>
                <br />
                Estado: {raid.estado}
                <br />
                🛡️ T:{raid.cuposTanque} | 💚 H:{raid.cuposHealer} | ⚔️ DPS:
                {raid.cuposDps}
                <hr style={{ borderColor: "#444", margin: "5px 0" }} />
                {raid.estado === "Programada" ? (
                  <>
                    <button
                      onClick={() => inscribir(raid.idRaid)}
                      style={{
                        backgroundColor: "#61dafb",
                        color: "#000",
                        border: "none",
                        padding: "8px 16px",
                        borderRadius: "4px",
                        cursor: "pointer",
                        fontWeight: "bold",
                        width: "100%",
                      }}
                    >
                      ⚔️ Inscribirse
                    </button>
                    <p
                      style={{
                        fontSize: "11px",
                        color: "#888",
                        marginTop: "5px",
                      }}
                    >
                      Usando personaje activo:{" "}
                      {activeId ? `ID ${activeId}` : "Ninguno"}
                    </p>
                  </>
                ) : (
                  <p style={{ color: "#f44336", fontSize: "12px" }}>
                    🔒 Raid cerrada
                  </p>
                )}
              </div>
            </Popup>
          </Marker>
        );
      })}
    </MapContainer>
  );
};

export default MapaRaids;
