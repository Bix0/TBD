import { useState, useEffect } from "react";
import api from "../services/api"; // Usamos tu interceptor para evitar el Error 403
import Navbar from "../components/Navbar";

function Inventario() {
  const [itemsInventario, setItemsInventario] = useState([]);
  const [catalogo, setCatalogo] = useState([]);

  const idPersonajeActivo = localStorage.getItem("activePersonajeId");

  const cargarDatos = async () => {
    if (!idPersonajeActivo || idPersonajeActivo === "null") return;
    try {
      const resItems = await api.get("/api/items");
      const resInv = await api.get(
        `/api/personajes/${idPersonajeActivo}/inventario`,
      );
      setCatalogo(resItems.data);
      setItemsInventario(resInv.data);
    } catch (error) {
      console.error("Error al cargar inventario:", error);
    }
  };

  useEffect(() => {
    cargarDatos();
  }, [idPersonajeActivo]);

  const manejarEquipar = async (idInventario, estaEquipado) => {
    try {
      if (!estaEquipado) {
        const itemPuesto = itemsInventario.find((i) => i.equipado === true);
        if (itemPuesto) {
          const idDesequipar =
            itemPuesto.id_inventario || itemPuesto.idInventario;
          await api.put(
            `/api/personajes/${idPersonajeActivo}/inventario/${idDesequipar}/desequipar`,
          );
        }
      }

      const accion = estaEquipado ? "desequipar" : "equipar";
      await api.put(
        `/api/personajes/${idPersonajeActivo}/inventario/${idInventario}/${accion}`,
      );

      cargarDatos();
    } catch (error) {
      alert("Error al intentar equipar el arma.");
    }
  };

  const tirarObjeto = (idInventario) => {
    if (
      window.confirm(
        "¿Seguro que deseas botar este ítem? Se perderá para siempre.",
      )
    ) {
      api
        .delete(
          `/api/personajes/${idPersonajeActivo}/inventario/${idInventario}`,
        )
        .then(() => cargarDatos());
    }
  };

  return (
    <div
      style={{ backgroundColor: "#121212", minHeight: "100vh", color: "white" }}
    >
      <Navbar />
      <div style={{ padding: "20px", maxWidth: "800px", margin: "0 auto" }}>
        <h1
          style={{
            textAlign: "center",
            color: "#ff9800",
            marginBottom: "30px",
          }}
        >
          🎒 Mi Mochila
        </h1>

        <div style={{ display: "grid", gap: "15px" }}>
          {!idPersonajeActivo || idPersonajeActivo === "null" ? (
            <div
              style={{
                textAlign: "center",
                padding: "40px",
                backgroundColor: "#1a1a1a",
                borderRadius: "8px",
              }}
            >
              <p style={{ color: "#f44336" }}>
                Ningún personaje seleccionado. Ve a "Mis Personajes" y usa uno.
              </p>
            </div>
          ) : itemsInventario.length === 0 ? (
            <div
              style={{
                textAlign: "center",
                padding: "40px",
                backgroundColor: "#1a1a1a",
                borderRadius: "8px",
              }}
            >
              <p style={{ color: "#888" }}>Tu mochila está vacía.</p>
            </div>
          ) : (
            itemsInventario.map((inv, index) => {
              // El backend serializa el campo como 'itemId' (camelCase) en el documento Inventario
              const idItemMochila =
                inv.itemId ||
                inv.id_item ||
                inv.idItem ||
                (inv.item && (inv.item.idItem || inv.item.id_item));
              const idMochila = inv.id_inventario || inv.idInventario || index;

              const dataItem = catalogo.find(
                (c) => String(c.id_item || c.idItem || c._id) === String(idItemMochila),
              ) || { nombre: "Ítem Desconocido", itemLvl: 0, item_lvl: 0, gananciaDkp: 0, ganancia_dkp: 0 };

              const cantidadObjeto = inv.cantidad || 1;

              return (
                <div
                  key={idMochila}
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    padding: "20px",
                    backgroundColor: inv.equipado ? "#1e3320" : "#1a1a1a",
                    border: inv.equipado
                      ? "1px solid #4caf50"
                      : "1px solid #444",
                    borderRadius: "8px",
                  }}
                >
                  <div>
                    <h3
                      style={{
                        margin: "0 0 8px 0",
                        color: inv.equipado ? "#81c784" : "#fff",
                      }}
                    >
                      {dataItem.nombre} {inv.equipado && "(Equipado)"} {cantidadObjeto > 1 && <span style={{ color: '#ffca28', fontSize: '15px', marginLeft: '6px' }}>x{cantidadObjeto}</span>}
                    </h3>
                    <div style={{ display: "flex", gap: "15px" }}>
                      <span
                        style={{
                          backgroundColor: "#333",
                          padding: "4px 8px",
                          borderRadius: "4px",
                          fontSize: "13px",
                          color: "#ff9800",
                        }}
                      >
                        ⚔️ Poder: +{dataItem.itemLvl ?? dataItem.item_lvl ?? 0}
                      </span>
                      <span
                        style={{
                          backgroundColor: "#333",
                          padding: "4px 8px",
                          borderRadius: "4px",
                          fontSize: "13px",
                          color: "#61dafb",
                        }}
                      >
                        💎 Valor DKP:{" "}
                        {dataItem.gananciaDkp ?? dataItem.ganancia_dkp ?? 0}
                      </span>
                      <span
                        style={{
                          backgroundColor: "#333",
                          padding: "4px 8px",
                          borderRadius: "4px",
                          fontSize: "13px",
                          color: "#ffca28",
                        }}
                      >
                        📦 Cantidad: {cantidadObjeto}
                      </span>
                    </div>
                  </div>

                  <div style={{ display: "flex", gap: "10px" }}>
                    <button
                      onClick={() =>
                        manejarEquipar(
                          inv.id_inventario || inv.idInventario,
                          inv.equipado,
                        )
                      }
                      style={{
                        padding: "10px 20px",
                        color: "white",
                        border: "none",
                        borderRadius: "4px",
                        cursor: "pointer",
                        fontWeight: "bold",
                        backgroundColor: inv.equipado ? "#f44336" : "#4caf50",
                      }}
                    >
                      {inv.equipado ? "Desequipar" : "Equipar"}
                    </button>
                    <button
                      onClick={() =>
                        tirarObjeto(inv.id_inventario || inv.idInventario)
                      }
                      style={{
                        padding: "10px",
                        color: "white",
                        border: "none",
                        borderRadius: "4px",
                        cursor: "pointer",
                        backgroundColor: "#555",
                      }}
                    >
                      🗑️ Botar
                    </button>
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
}

export default Inventario;
