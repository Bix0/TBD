import { useState, useEffect } from "react";
import axios from "axios";
import Navbar from "../components/Navbar";

function Ranking() {
  const [personajes, setPersonajes] = useState([]);
  const [clanesRanking, setClanesRanking] = useState([]);

  const miUserId = String(localStorage.getItem("userId") || "");

  useEffect(() => {
    const token = localStorage.getItem("token");
    const headers = { Authorization: `Bearer ${token}` };

    // Ranking global de personajes por DKP
    axios
      .get("/api/personajes", { headers })
      .then((response) => {
        const rankingOrdenado = response.data.sort((a, b) => {
          const puntosA = a.puntos_merito ?? a.puntosMerito ?? 0;
          const puntosB = b.puntos_merito ?? b.puntosMerito ?? 0;
          return puntosB - puntosA;
        });
        setPersonajes(rankingOrdenado);
      })
      .catch((error) => console.error("Error cargando el ranking:", error));

    // Ranking por clan: resultado del Aggregation Pipeline (Lab3) con
    // $lookup + $group + $sort (daño total, asistencia, tiempo de finalización)
    axios
      .get("/api/ranking/clanes-desempeno", { headers })
      .then((res) => setClanesRanking(res.data || []))
      .catch((error) =>
        console.error("Error cargando ranking de clanes:", error),
      );
  }, []);

  const miIndice = personajes.findIndex(
    (p) =>
      String(p.id_jugador || p.jugadorId || p.idJugador || "") === miUserId,
  );
  const miPosicionActual = miIndice !== -1 ? miIndice + 1 : null;

  return (
    <div
      style={{ backgroundColor: "#121212", minHeight: "100vh", color: "white" }}
    >
      <Navbar />
      <div style={{ padding: "20px", maxWidth: "800px", margin: "0 auto" }}>
        <h1
          style={{
            textAlign: "center",
            color: "#61dafb",
            marginBottom: "10px",
          }}
        >
          🏆 Ranking DKP Global
        </h1>

        {miPosicionActual && (
          <div
            style={{
              textAlign: "center",
              marginBottom: "30px",
              fontSize: "18px",
              color: "#aaa",
            }}
          >
            Tu posición actual es:{" "}
            <strong style={{ color: "#ff9800", fontSize: "24px" }}>
              #{miPosicionActual}
            </strong>
          </div>
        )}

        <div
          style={{
            backgroundColor: "#1a1a1a",
            borderRadius: "8px",
            border: "1px solid #333",
            overflow: "hidden",
          }}
        >
          <div
            style={{
              display: "flex",
              backgroundColor: "#242424",
              padding: "15px",
              fontWeight: "bold",
              borderBottom: "2px solid #444",
            }}
          >
            <div style={{ width: "10%" }}>#</div>
            <div style={{ width: "40%" }}>Personaje</div>
            <div style={{ width: "25%" }}>Clase</div>
            <div style={{ width: "25%", textAlign: "right", color: "#ff9800" }}>
              Puntos (DKP)
            </div>
          </div>

          {personajes.length === 0 ? (
            <div
              style={{ padding: "20px", textAlign: "center", color: "#888" }}
            >
              Cargando gladiadores...
            </div>
          ) : (
            personajes.map((personaje, index) => {
              const nombre = personaje.nombre;
              const clase = personaje.clase;
              const puntos =
                personaje.puntos_merito || personaje.puntosMerito || 0;

              const esMiPersonaje =
                personaje.id_jugador === miUserId ||
                personaje.idJugador === miUserId ||
                personaje.jugador?.id === miUserId;

              let colorPosicion = "#aaa";
              if (index === 0) colorPosicion = "#ffd700";
              if (index === 1) colorPosicion = "#c0c0c0";
              if (index === 2) colorPosicion = "#cd7f32";

              return (
                <div
                  key={personaje.id_personaje || personaje.idPersonaje || index}
                  style={{
                    display: "flex",
                    padding: "15px",
                    borderBottom: "1px solid #333",
                    alignItems: "center",
                    backgroundColor: esMiPersonaje ? "#2a2a2a" : "transparent",
                    borderLeft: esMiPersonaje
                      ? "4px solid #61dafb"
                      : "4px solid transparent",
                  }}
                >
                  <div
                    style={{
                      width: "10%",
                      fontSize: "20px",
                      fontWeight: "bold",
                      color: colorPosicion,
                    }}
                  >
                    {index + 1}
                  </div>
                  <div
                    style={{
                      width: "40%",
                      fontWeight: "bold",
                      color: esMiPersonaje ? "#61dafb" : "#fff",
                    }}
                  >
                    {nombre}{" "}
                    {esMiPersonaje && (
                      <span
                        style={{
                          color: "#ff9800",
                          fontSize: "12px",
                          marginLeft: "5px",
                        }}
                      >
                        (Tú)
                      </span>
                    )}
                  </div>
                  <div style={{ width: "25%", color: "#888" }}>{clase}</div>
                  <div
                    style={{
                      width: "25%",
                      textAlign: "right",
                      fontWeight: "bold",
                      color: "#ff9800",
                      fontSize: "18px",
                    }}
                  >
                    {puntos}
                  </div>
                </div>
              );
            })
          )}
        </div>

        {/* ========================================= */}
        {/* RANKING POR CLAN (Aggregation Pipeline Lab3) */}
        {/* ========================================= */}
        <h1
          style={{
            textAlign: "center",
            color: "#aa3bff",
            margin: "50px 0 6px",
            lineHeight: 1.3,
          }}
        >
          🏰 Ranking por Clan
        </h1>
        <p
          style={{
            textAlign: "center",
            color: "#aaa",
            fontSize: "14px",
            margin: "0 0 20px",
            lineHeight: 1.4,
          }}
        >
          Desempeño en raids · Aggregation Pipeline ($lookup + $group + $sort):
          daño total, asistencia y tiempo promedio de finalización.
        </p>

        <div
          style={{
            backgroundColor: "#1a1a1a",
            borderRadius: "8px",
            border: "1px solid #333",
            overflow: "hidden",
          }}
        >
          <div
            style={{
              display: "flex",
              backgroundColor: "#242424",
              padding: "15px",
              fontWeight: "bold",
              borderBottom: "2px solid #444",
            }}
          >
            <div style={{ width: "30%" }}>Clan</div>
            <div style={{ width: "25%" }}>Daño Total</div>
            <div style={{ width: "20%" }}>Asistencia</div>
            <div
              style={{
                width: "25%",
                textAlign: "right",
                color: "#ff9800",
              }}
            >
              Tiempo Prom. (min)
            </div>
          </div>

          {clanesRanking.length === 0 ? (
            <div
              style={{ padding: "20px", textAlign: "center", color: "#888" }}
            >
              Sin raids completadas todavía. ¡Simula una raid para ver el
              desempeño!
            </div>
          ) : (
            clanesRanking.map((c, index) => (
              <div
                key={index}
                style={{
                  display: "flex",
                  padding: "15px",
                  borderBottom: "1px solid #333",
                  alignItems: "center",
                }}
              >
                <div
                  style={{ width: "30%", fontWeight: "bold", color: "#61dafb" }}
                >
                  {c.nombreClan || c._id || "Clan"}
                </div>
                <div
                  style={{
                    width: "25%",
                    fontWeight: "bold",
                    color: "#ff9800",
                  }}
                >
                  {c.danoTotal ?? 0}
                </div>
                <div style={{ width: "20%", color: "#81c784" }}>
                  {c.asistenciaTotal ?? 0}
                </div>
                <div
                  style={{ width: "25%", textAlign: "right", color: "#888" }}
                >
                  {Number(c.tiempoPromedioRaid ?? 0).toFixed(1)}
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}

export default Ranking;
