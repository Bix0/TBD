import React, { useEffect, useState } from "react";
import { MapContainer, ImageOverlay, Marker, Popup } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import api from "../services/api";

const bossIcon = new L.Icon({
  iconUrl: "https://cdn-icons-png.flaticon.com/512/3593/3593502.png",
  iconSize: [40, 40],
  iconAnchor: [20, 40],
  popupAnchor: [0, -40],
});

const MapaRaids = () => {
  const [raids, setRaids] = useState([]);
  const [personajes, setPersonajes] = useState([]);
  const [selectedPersonaje, setSelectedPersonaje] = useState("");
  const userId = localStorage.getItem("userId");
  const activeId = localStorage.getItem("activePersonajeId");

  useEffect(() => {
    api
      .get("/api/raids/cercanas", {
        params: { lon: 500, lat: 500, distancia: 2000 },
      })
      .then((response) => setRaids(response.data || []))
      .catch((error) => console.error("Error cargando raids:", error));

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
      <ImageOverlay url="/mapa-juego.jpg" bounds={bounds} />

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
