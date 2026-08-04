package com.grupo3.mmorpg.repositories;

import com.grupo3.mmorpg.models.Item;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de MongoDB para la entidad Item
 */
@Repository
public interface ItemRepository extends MongoRepository<Item, String> {

    Optional<Item> findByNombre(String nombre);

    // Consulta adaptada a la sintaxis JSON de MongoDB ($gte para mayor o igual, sort para ordenar)
    @Query(value = "{ 'itemLvl': { $gte: ?0 } }", sort = "{ 'itemLvl': -1 }")
    List<Item> findByItemLevelMin(Integer itemLevel);
}