package com.grupo3.mmorpg.repositories;

import com.grupo3.mmorpg.models.ItemClasePermitida;
import com.grupo3.mmorpg.models.ItemClasePermitida.ItemClasePermitidaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad ItemClasePermitida (clave compuesta)
 */
@Repository
public interface ItemClasePermitidaRepository extends JpaRepository<ItemClasePermitida, ItemClasePermitidaId> {

    List<ItemClasePermitida> findByItemIdItem(Long idItem);

    @Query("SELECT icp.id.clasePermitida FROM ItemClasePermitida icp WHERE icp.item.idItem = :idItem")
    List<String> findClasesPermitidasByItemId(@Param("idItem") Long idItem);

    @Query("SELECT COUNT(icp) > 0 FROM ItemClasePermitida icp WHERE icp.item.idItem = :idItem AND icp.id.clasePermitida = :clase")
    boolean existsByItemIdAndClasePermitida(@Param("idItem") Long idItem, @Param("clase") String clase);

    void deleteByItemIdItemAndIdClasePermitida(Long idItem, String clasePermitida);
}
