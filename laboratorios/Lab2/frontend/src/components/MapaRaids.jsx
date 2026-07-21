import React, { useEffect, useState } from "react";
import {
  MapContainer,
  ImageOverlay,
  Marker,
  Popup,
  useMap,
  useMapEvents,
} from "react-leaflet";
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

// Icono genérico para otros jugadores en el mapa
const otherPlayerIcon = new L.Icon({
  iconUrl: "/player_icon.png", // Puedes cambiarlo si tienes otro ícono, ej. /other_player.png
  iconSize: [32, 32],
  iconAnchor: [16, 32],
  popupAnchor: [0, -32],
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

function PlayerMovementController({ activePersonajeId, onMove }) {
  useMapEvents({
    click(e) {
      if (!activePersonajeId) return;
      const { lat, lng } = e.latlng;
      const roundedLat = Math.round(lat);
      const roundedLng = Math.round(lng);

      api
        .patch(
          `/api/personajes/${activePersonajeId}/mover?latitud=${roundedLat}&longitud=${roundedLng}`,
        )
        .then(() => {
          onMove(activePersonajeId, roundedLat, roundedLng);
        })
        .catch((err) => console.error("Error moviendo personaje:", err));
    },
  });
  return null;
}

const MapaRaids = () => {
  const [raids, setRaids] = useState([]);
  const [personajes, setPersonajes] = useState([]); // Personajes del usuario actual
  const [todosPersonajesMapa, setTodosPersonajesMapa] = useState([]); // Todos los personajes geolocalizados
  const [selectedPersonaje, setSelectedPersonaje] = useState("");
  const [mapCenter, setMapCenter] = useState({ lat: 500, lng: 500 });

  // Estados para Filtros
  const [filtroNombre, setFiltroNombre] = useState("");
  const [filtroRol, setFiltroRol] = useState("");

  const userId = localStorage.getItem("userId");
  const activeId = localStorage.getItem("activePersonajeId");

  // Cargar Raids cercanas
  useEffect(() => {
    const personajeId = activeId || selectedPersonaje;

    api
      .get("/api/raids/cercanas", {
        params: {
          idPersonaje: personajeId || undefined,
          lon: mapCenter.lng,
          lat: mapCenter.lat,
          distancia: 2000,
        },
      })
      .then((response) => setRaids(response.data || []))
      .catch((error) => console.error("Error cargando raids:", error));
  }, [activeId, mapCenter.lat, mapCenter.lng, selectedPersonaje]);

  // Cargar personajes del usuario logueado
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

  // Cargar todos los personajes con ubicación para el mapa general
  useEffect(() => {
    api
      .get("/api/personajes/mapa")
      .then((res) => {
        setTodosPersonajesMapa(res.data || []);
      })
      .catch((err) =>
        console.error("Error cargando personajes en el mapa:", err),
      );
  }, []);

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

  const handlePersonajeMoved = (id, lat, lng) => {
    setPersonajes((prev) =>
      prev.map((p) => {
        if ((p.idPersonaje || p.id_personaje) == id) {
          return { ...p, latitud: lat, longitud: lng };
        }
        return p;
      }),
    );

    // Actualizar también en la lista general del mapa
    setTodosPersonajesMapa((prev) =>
      prev.map((p) => {
        if ((p.idPersonaje || p.id_personaje) == id) {
          return { ...p, latitud: lat, longitud: lng };
        }
        return p;
      }),
    );

    setMapCenter({ lat, lng });
  };

  // Filtrar personajes para el mapa según los criterios de búsqueda
  const personajesFiltrados = todosPersonajesMapa.filter((p) => {
    const nombre = p.nombre || "";
    const rol = p.rolClan || p.rol_clan || "";

    const cumpleNombre = nombre
      .toLowerCase()
      .includes(filtroNombre.toLowerCase());
    const cumpleRol =
      filtroRol === "" || rol.toLowerCase().includes(filtroRol.toLowerCase());

    return cumpleNombre && cumpleRol;
  });

  const bounds = [
    [0, 0],
    [1000, 1000],
  ];

  return (
    <div style={{ position: "relative" }}>
      {/* Panel flotante de Filtros */}
      <div
        style={{
          position: "absolute",
          top: "15px",
          right: "15px",
          zIndex: 1000,
          backgroundColor: "rgba(0, 0, 0, 0.85)",
          padding: "12px 16px",
          borderRadius: "8px",
          border: "1px solid #444",
          color: "#fff",
          boxShadow: "0 4px 10px rgba(0,0,0,0.5)",
          display: "flex",
          gap: "10px",
          alignItems: "center",
        }}
      >
        <strong style={{ color: "#61dafb", fontSize: "14px" }}>
          🔍 Filtros Mapa:
        </strong>

        <input
          type="text"
          placeholder="Buscar por nombre..."
          value={filtroNombre}
          onChange={(e) => setFiltroNombre(e.target.value)}
          style={{
            padding: "6px 10px",
            borderRadius: "4px",
            border: "1px solid #666",
            backgroundColor: "#222",
            color: "#fff",
            fontSize: "13px",
          }}
        />

        <select
          value={filtroRol}
          onChange={(e) => setFiltroRol(e.target.value)}
          style={{
            padding: "6px 10px",
            borderRadius: "4px",
            border: "1px solid #666",
            backgroundColor: "#222",
            color: "#fff",
            fontSize: "13px",
          }}
        >
          <option value="">Todos los Roles</option>
          <option value="Tanque">Tanque</option>
          <option value="Healer">Healer</option>
          <option value="DPS">DPS</option>
        </select>
      </div>

      <MapContainer
        crs={L.CRS.Simple}
        bounds={bounds}
        maxBounds={bounds}
        maxBoundsViscosity={1.0}
        style={{ height: "700px", width: "100%", backgroundColor: "#000" }}
        className="leaflet-container"
      >
        <RaidMapController onCenterChange={setMapCenter} />
        <PlayerMovementController
          activePersonajeId={activeId || selectedPersonaje}
          onMove={handlePersonajeMoved}
        />
        <ImageOverlay url="/mapa_juego.png" bounds={bounds} />

        {/* Renderizar todos los demás personajes filtrados en el mapa */}
        {personajesFiltrados.map((p) => {
          const pId = p.idPersonaje || p.id_personaje;
          const activeCurrentId = activeId || selectedPersonaje;

          // Si es el jugador activo, ya se renderiza abajo con su icono especial o se puede omitir para evitar duplicado
          if (pId == activeCurrentId) return null;

          const lat = p.latitud;
          const lon = p.longitud;

          if (lat == null || lon == null) return null;

          return (
            <Marker
              key={`p-${pId}`}
              position={[lat, lon]}
              icon={otherPlayerIcon}
            >
              <Popup>
                <div style={{ textAlign: "center" }}>
                  <strong style={{ color: "#ffaa00", fontSize: "15px" }}>
                    🛡️ {p.nombre}
                  </strong>
                  <br />
                  Clase: {p.clase} | Rol: {p.rolClan || p.rol_clan}
                  <br />
                  Nivel: {p.nivel} | Poder: {p.itemLevel || p.item_level}
                </div>
              </Popup>
            </Marker>
          );
        })}

        {/* Renderizar jugador activo (Tú) */}
        {(() => {
          const activePersonaje = personajes.find(
            (p) =>
              (p.idPersonaje || p.id_personaje) ==
              (activeId || selectedPersonaje),
          );
          if (
            activePersonaje &&
            activePersonaje.latitud != null &&
            activePersonaje.longitud != null
          ) {
            return (
              <Marker
                position={[activePersonaje.latitud, activePersonaje.longitud]}
                icon={playerIcon}
              >
                <Popup>
                  <div style={{ textAlign: "center" }}>
                    <strong style={{ color: "#61dafb", fontSize: "16px" }}>
                      🧑 {activePersonaje.nombre} (Tú)
                    </strong>
                    <br />
                    Nivel: {activePersonaje.nivel} | Poder:{" "}
                    {activePersonaje.itemLevel || activePersonaje.item_level}
                  </div>
                </Popup>
              </Marker>
            );
          }
          return null;
        })()}

        {/* Renderizar Raids y Bosses */}
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
    </div>
  );
};

export default MapaRaids;
