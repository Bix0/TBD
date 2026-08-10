package com.grupo3.mmorpg.repositories;

import com.grupo3.mmorpg.models.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Inventario
 */
@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    List<Inventario> findByPersonajeIdPersonaje(Long personajeId);

    List<Inventario> findByPersonajeIdPersonajeAndEquipadoTrue(Long personajeId);

    Optional<Inventario> findByPersonajeIdPersonajeAndItemIdItem(Long personajeId, Long itemId);

    @Modifying
    @Query("UPDATE Inventario i SET i.equipado = TRUE WHERE i.idInventario = :idInventario")
    int equiparItem(@Param("idInventario") Long idInventario);

    @Modifying
    @Query("UPDATE Inventario i SET i.equipado = FALSE WHERE i.idInventario = :idInventario")
    int desequiparItem(@Param("idInventario") Long idInventario);

    @Modifying
    @Query("UPDATE Inventario i SET i.cantidad = i.cantidad + :cantidad WHERE i.idInventario = :idInventario")
    int aumentarCantidad(@Param("idInventario") Long idInventario, @Param("cantidad") Integer cantidad);

    @Query("SELECT COUNT(i) > 0 FROM Inventario i WHERE i.personaje.idPersonaje = :personajeId AND i.item.idItem = :itemId")
    boolean tieneItem(@Param("personajeId") Long personajeId, @Param("itemId") Long itemId);

    @Query("SELECT COUNT(i) FROM Inventario i WHERE i.personaje.idPersonaje = :personajeId")
    int contarItems(@Param("personajeId") Long personajeId);
}
