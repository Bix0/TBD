package com.grupo3.mmorpg.repositories;

import com.grupo3.mmorpg.models.Item;
import com.grupo3.mmorpg.models.ItemClasePermitida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Item
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findByNombre(String nombre);

    @Query("SELECT i FROM Item i WHERE i.itemLvl >= :itemLevel ORDER BY i.itemLvl DESC")
    List<Item> findByItemLevelMin(@Param("itemLevel") Integer itemLevel);
}
