import React, { useEffect, useState } from "react";
import {
  MapContainer,
  ImageOverlay,
  CircleMarker,
  Marker,
  Popup,
  Tooltip,
} from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import api from "../services/api";

const playerIcon = new L.Icon({
  iconUrl: "/player_icon.png",
  iconSize: [40, 40],
  iconAnchor: [20, 40],
  popupAnchor: [0, -40],
});

const currentIcon = new L.Icon({
  iconUrl: "/sede_current.png",
  iconSize: [32, 32],
  iconAnchor: [16, 32],
  popupAnchor: [0, -32],
});

const oldIcon = new L.Icon({
  iconUrl: "/sede_old.png",
  iconSize: [24, 24],
  iconAnchor: [12, 24],
  popupAnchor: [0, -24],
});

const MapaClanes = ({ auditoria = [], liderAlianza, liderHorda }) => {
  const [modo, setModo] = useState("calor"); // "calor" | "cercanos" | "sedes"
  const [clanesCalor, setClanesCalor] = useState([]);
  const [clanesCercanos, setClanesCercanos] = useState([]);
  const [personajes, setPersonajes] = useState([]);
  const [activePj, setActivePj] = useState(null);

  // Filtros para clanes cercanos (solo se puede elegir el radio de distancia)
  const [distancia, setDistancia] = useState(500);
  const [cargandoCercanos, setCargandoCercanos] = useState(false);

  const userId = localStorage.getItem("userId");
  const activeId = localStorage.getItem("activePersonajeId");

  // Cargar lista de personajes del jugador para detectar el personaje activo y sus datos
  useEffect(() => {
    if (userId) {
      api
        .get(`/api/personajes/jugador/${userId}/todos`)
        .then((res) => {
          const list = res.data || [];
          setPersonajes(list);
          const found = list.find(
            (p) => (p.idPersonaje || p.id_personaje) == activeId,
          );
          if (found) {
            setActivePj(found);
          }
        })
        .catch((err) => console.error("Error cargando personajes:", err));
    }
  }, [userId, activeId]);

  // Cargar mapa de calor
  const cargarMapaCalor = () => {
    api
      .get("/api/clanes/mapa-calor")
      .then((res) => setClanesCalor(res.data || []))
      .catch((err) => console.error("Error cargando mapa de calor:", err));
  };

  // Cargar clanes cercanos (PostGIS ST_DWithin) usando exclusivamente las coordenadas y facción del PJ activo
  const cargarClanesCercanos = () => {
    if (!activePj || activePj.latitud == null || activePj.longitud == null) {
      setClanesCercanos([]);
      return;
    }

    setCargandoCercanos(true);
    const params = {
      lat: activePj.latitud,
      lon: activePj.longitud,
      distancia: distancia,
      faccion: activePj.faccion, // La facción se toma automáticamente del personaje
    };

    api
      .get("/api/clanes/cercanos", { params })
      .then((res) => {
        setClanesCercanos(res.data || []);
      })
      .catch((err) => {
        setClanesCercanos([]);
      })
      .finally(() => setCargandoCercanos(false));
  };

  useEffect(() => {
    if (modo === "calor") {
      cargarMapaCalor();
    } else {
      cargarClanesCercanos();
    }
  }, [modo, activePj, distancia]);

  const unirseAlClan = (clan) => {
    if (!activePj) {
      alert("Debes seleccionar un personaje activo para unirte a un clan.");
      return;
    }
    const pjId = activePj.idPersonaje || activePj.id_personaje;
    const clanId = clan.idClan || clan.id_clan;

    if (
      activePj.clan &&
      (activePj.clan.idClan || activePj.clan.id_clan) == clanId
    ) {
      alert("¡Ya perteneces a este clan!");
      return;
    }

    if (
      window.confirm(
        `¿Deseas que ${activePj.nombre} se una al clan "${clan.nombre}"?`,
      )
    ) {
      api
        .post(`/api/clanes/unirse/${clanId}`, pjId, {
          headers: { "Content-Type": "application/json" },
        })
        .then(() => {
          alert(
            `¡${activePj.nombre} se ha unido exitosamente al clan ${clan.nombre}!`,
          );
          if (userId) {
            api.get(`/api/personajes/jugador/${userId}/todos`).then((res) => {
              const list = res.data || [];
              setPersonajes(list);
              const found = list.find(
                (p) => (p.idPersonaje || p.id_personaje) == activeId,
              );
              if (found) setActivePj(found);
            });
          }
          cargarClanesCercanos();
        })
        .catch((err) => {
          console.error("Error al unirse al clan:", err);
          alert("No se pudo unirse al clan.");
        });
    }
  };

  const calcularRadioCalor = (dkp) => {
    const base = 12;
    const extra = Math.min(dkp / 50, 45);
    return base + extra;
  };

  // Límites del plano 1000x1000 [Y, X]
  const bounds = [
    [0, 0],
    [1000, 1000],
  ];

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: "15px" }}>
      {/* PANEL DE CONTROL / SELECTOR DE MODO */}
      <div
        style={{
          backgroundColor: "#242424",
          padding: "15px",
          borderRadius: "8px",
          border: "1px solid #444",
          display: "flex",
          flexWrap: "wrap",
          gap: "15px",
          alignItems: "center",
          justifyContent: "space-between",
        }}
      >
        {/* Selector de Modo */}
        <div style={{ display: "flex", gap: "10px", alignItems: "center" }}>
          <label
            style={{ fontWeight: "bold", color: "#aaa", fontSize: "14px" }}
          >
            Visualización:
          </label>
          <button
            onClick={() => setModo("calor")}
            style={{
              padding: "8px 16px",
              backgroundColor: modo === "calor" ? "#ff4b4b" : "#333",
              color: "white",
              border: "none",
              borderRadius: "4px",
              fontWeight: "bold",
              cursor: "pointer",
              transition: "0.2s",
            }}
          >
            🔥 Mapa de Calor (DKP)
          </button>
          <button
            onClick={() => setModo("cercanos")}
            style={{
              padding: "8px 16px",
              backgroundColor: modo === "cercanos" ? "#2196f3" : "#333",
              color: "white",
              border: "none",
              borderRadius: "4px",
              fontWeight: "bold",
              cursor: "pointer",
              transition: "0.2s",
            }}
          >
            📍 Clanes Cercanos (Radio GPS)
          </button>
          <button
            onClick={() => setModo("sedes")}
            style={{
              padding: "8px 16px",
              backgroundColor: modo === "sedes" ? "#ffd700" : "#333",
              color: modo === "sedes" ? "#000" : "white",
              border: "none",
              borderRadius: "4px",
              fontWeight: "bold",
              cursor: "pointer",
              transition: "0.2s",
            }}
          >
            👑 Sedes de Poder
          </button>
        </div>

        {/* Único filtro permitido en modo Clanes Cercanos: Radio de Distancia */}
        {modo === "cercanos" && (
          <div
            style={{
              display: "flex",
              gap: "15px",
              flexWrap: "wrap",
              alignItems: "center",
            }}
          >
            {activePj && (
              <div
                style={{
                  fontSize: "13px",
                  color: "#61dafb",
                  backgroundColor: "#1e293b",
                  padding: "6px 12px",
                  borderRadius: "6px",
                  border: "1px solid #3b82f6",
                }}
              >
                🧑 <b>{activePj.nombre}</b> ({activePj.faccion}) | Posición: Y=
                {activePj.latitud}, X={activePj.longitud}
              </div>
            )}
            <div>
              <label
                style={{
                  fontSize: "12px",
                  color: "#aaa",
                  display: "block",
                  marginBottom: "2px",
                }}
              >
                Radio Distancia (Uds)
              </label>
              <input
                type="number"
                value={distancia}
                onChange={(e) =>
                  setDistancia(Math.max(10, parseInt(e.target.value) || 0))
                }
                step="50"
                min="10"
                max="2000"
                style={{
                  width: "110px",
                  padding: "6px",
                  backgroundColor: "#333",
                  color: "white",
                  border: "1px solid #555",
                  borderRadius: "4px",
                }}
              />
            </div>
          </div>
        )}
      </div>

      {/* MENSAJE DE ADVERTENCIA SI NO HAY PERSONAJE ACTIVO EN MODO CERCANOS */}
      {modo === "cercanos" && !activePj && (
        <div
          style={{
            backgroundColor: "#332a00",
            border: "1px solid #ffcc00",
            color: "#ffea79",
            padding: "12px",
            borderRadius: "6px",
            textAlign: "center",
            fontSize: "14px",
          }}
        >
          ⚠️ No tienes un personaje activo seleccionado. Ve a la sección{" "}
          <b>Personajes</b> para activar uno y buscar clanes cercanos a su
          ubicación.
        </div>
      )}

      {/* MAPA LEAFLET */}
      <MapContainer
        crs={L.CRS.Simple}
        bounds={bounds}
        maxBounds={bounds}
        maxBoundsViscosity={1.0}
        style={{
          height: "650px",
          width: "100%",
          borderRadius: "8px",
          overflow: "hidden",
          backgroundColor: "#000",
        }}
        className="leaflet-container"
      >
        <ImageOverlay url="/mapa_juego.png" bounds={bounds} />

        {/* Renderizar Jugador Activo */}
        {activePj && activePj.latitud != null && activePj.longitud != null && (
          <Marker
            position={[activePj.latitud, activePj.longitud]}
            icon={playerIcon}
          >
            <Popup>
              <div style={{ textAlign: "center" }}>
                <strong style={{ color: "#61dafb", fontSize: "16px" }}>
                  🧑 {activePj.nombre} (Tú)
                </strong>
                <br />
                Facción: {activePj.faccion} | Nivel: {activePj.nivel}
                <br />
                Poder:{" "}
                <b>{activePj.itemLevel ?? activePj.item_level ?? 0} iLvl</b> |
                Rol:{" "}
                <b>
                  {activePj.rolClan ||
                    activePj.rol_clan ||
                    activePj.rol ||
                    "Sin Rol"}
                </b>
                <br />
                Coordenadas: Y={activePj.latitud}, X={activePj.longitud}
              </div>
            </Popup>
          </Marker>
        )}

        {/* MODO 1: MAPA DE CALOR */}
        {modo === "calor" &&
          clanesCalor.map((clan, index) => {
            const id = Array.isArray(clan) ? clan[0] : (clan.idClan || clan.id_clan);
            const nombre = Array.isArray(clan) ? clan[1] : clan.nombre;
            const lat = Array.isArray(clan) ? clan[2] : clan.latitud;
            const lon = Array.isArray(clan) ? clan[3] : clan.longitud;
            const dkpTotal = Array.isArray(clan) ? clan[4] : (clan.dkpTotal || clan.dkp_total || 0);

            return (
              <CircleMarker
                key={`calor-${index}`}
                center={[lat, lon]}
                pathOptions={{
                  color: "#ff4b4b",
                  fillColor: "#ff0000",
                  fillOpacity: 0.6,
                }}
                radius={calcularRadioCalor(dkpTotal)}
              >
                <Tooltip
                  direction="top"
                  offset={[0, -10]}
                  opacity={0.9}
                  permanent={false}
                >
                  <span style={{ fontWeight: "bold", color: "#aa3bff" }}>
                    {nombre} ({dkpTotal} DKP)
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

        {/* MODO 2: CLANES CERCANOS (ST_DWithin) */}
        {modo === "cercanos" &&
          activePj &&
          activePj.latitud != null &&
          activePj.longitud != null && (
            <>
              {/* Círculo visual mostrando el radio de búsqueda de PostGIS centrado en el personaje activo */}
              <CircleMarker
                center={[activePj.latitud, activePj.longitud]}
                pathOptions={{
                  color: activePj.faccion === "Alianza" ? "#2196f3" : "#f44336",
                  fillColor:
                    activePj.faccion === "Alianza" ? "#2196f3" : "#f44336",
                  fillOpacity: 0.15,
                  dashArray: "6, 6",
                }}
                radius={distancia / 2}
              >
                <Tooltip permanent direction="bottom">
                  <span
                    style={{
                      fontSize: "11px",
                      color:
                        activePj.faccion === "Alianza" ? "#2196f3" : "#ff4b4b",
                    }}
                  >
                    Radio de búsqueda: {distancia} uds ({activePj.faccion})
                  </span>
                </Tooltip>
              </CircleMarker>

              {/* Renderizar clanes cercanos de la misma facción devueltos por la API */}
              {clanesCercanos.map((clan, index) => {
                const lat = clan.latitud;
                const lon = clan.longitud;
                if (lat == null || lon == null) return null;

                const esAlianza = clan.faccion === "Alianza";
                const colorClan = esAlianza ? "#2196f3" : "#f44336";

                const clanId = clan.idClan || clan.id_clan;
                const yaEsMiembro =
                  activePj.clan &&
                  (activePj.clan.idClan || activePj.clan.id_clan) == clanId;

                return (
                  <CircleMarker
                    key={`cercanos-${clanId || index}`}
                    center={[lat, lon]}
                    pathOptions={{
                      color: colorClan,
                      fillColor: colorClan,
                      fillOpacity: 0.8,
                    }}
                    radius={18}
                  >
                    <Tooltip
                      direction="top"
                      offset={[0, -10]}
                      opacity={0.9}
                      permanent={false}
                    >
                      <span style={{ fontWeight: "bold", color: colorClan }}>
                        {esAlianza ? "🛡️" : "🪓"} {clan.nombre}
                      </span>
                    </Tooltip>
                    <Popup>
                      <div style={{ textAlign: "center", minWidth: "160px" }}>
                        <strong style={{ color: colorClan, fontSize: "16px" }}>
                          {clan.nombre}
                        </strong>
                        <hr style={{ borderColor: "#444", margin: "5px 0" }} />
                        Facción: <b>{clan.faccion}</b>
                        <br />
                        Coordenadas: Y={lat}, X={lon}
                        <hr style={{ borderColor: "#444", margin: "8px 0" }} />
                        {yaEsMiembro ? (
                          <span
                            style={{
                              color: "#4caf50",
                              fontWeight: "bold",
                              fontSize: "13px",
                            }}
                          >
                            ✅ Ya perteneces a este clan
                          </span>
                        ) : (
                          <button
                            onClick={() => unirseAlClan(clan)}
                            style={{
                              width: "100%",
                              padding: "6px 12px",
                              backgroundColor: "#2196f3",
                              color: "white",
                              border: "none",
                              borderRadius: "4px",
                              fontWeight: "bold",
                              cursor: "pointer",
                            }}
                          >
                            🏰 Unirse al Clan
                          </button>
                        )}
                      </div>
                    </Popup>
                  </CircleMarker>
                );
              })}
            </>
          )}

        {/* MODO 3: SEDES DE PODER */}
        {modo === "sedes" &&
          auditoria.map((fila, i) => {
            const lat = parseFloat(fila[5]);
            const lon = parseFloat(fila[6]);
            if (isNaN(lat) || isNaN(lon) || (lat === 0 && lon === 0))
              return null;
            return (
              <Marker
                key={`sede-${i}`}
                position={[lat, lon]}
                icon={
                  fila[3] === liderAlianza || fila[3] === liderHorda
                    ? currentIcon
                    : oldIcon
                }
              >
                <Popup>
                  <div style={{ textAlign: "center", minWidth: "180px" }}>
                    <strong style={{ color: "#ffd700", fontSize: "16px" }}>
                      👑 {fila[2]} → {fila[3]}
                    </strong>
                    <hr style={{ borderColor: "#444", margin: "5px 0" }} />
                    <b>Clan:</b> {fila[1]}
                    <br />
                    <b>Fecha:</b> {new Date(fila[4]).toLocaleString()}
                    <br />
                    <b>Ubicación:</b> Y={lat}, X={lon}
                  </div>
                </Popup>
              </Marker>
            );
          })}
      </MapContainer>

      {/* RESUMEN/INFO INFERIOR */}
      <div style={{ fontSize: "13px", color: "#aaa", textAlign: "center" }}>
        {modo === "calor" ? (
          <p>
            🔥 Mostrando <b>{clanesCalor.length}</b> clanes clasificados por
            volumen de DKP acumulado.
          </p>
        ) : modo === "cercanos" ? (
          <p>
            📍 Mostrando <b>{clanesCercanos.length}</b> clanes de la facción{" "}
            <b>{activePj?.faccion || "N/A"}</b> dentro de un radio de{" "}
            <b>{distancia}</b> uds desde tu posición (Y={activePj?.latitud}, X=
            {activePj?.longitud}).
          </p>
        ) : (
          <p>
            👑 Mostrando <b>{auditoria.filter((f) => f[5] != null).length}</b>{" "}
            sedes de poder registradas en cambios de liderazgo.
          </p>
        )}
      </div>
    </div>
  );
};

export default MapaClanes;
