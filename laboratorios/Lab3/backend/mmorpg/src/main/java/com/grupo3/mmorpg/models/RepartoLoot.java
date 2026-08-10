package com.grupo3.mmorpg.models;

/**
 * Representa la entrega de un ítem a un personaje dentro de una distribución
 * masiva de loot. Se recibe como JSON en el cuerpo de la petición.
 */
public record RepartoLoot(String idPersonaje, String idItem, Integer costoDkp) {
}
