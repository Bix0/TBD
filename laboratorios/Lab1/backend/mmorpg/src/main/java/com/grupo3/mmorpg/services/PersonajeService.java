package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.Personaje;
import com.grupo3.mmorpg.repositories.PersonajeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para operaciones de negocio relacionadas con Personajes
 * Utiliza PersonajeRepository para acceder a los datos
 */
@Service
public class PersonajeService {
    
    private final PersonajeRepository personajeRepository;
    
    public PersonajeService(PersonajeRepository personajeRepository) {
        this.personajeRepository = personajeRepository;
    }
    

    // CRUD
    /**
     * Crea un nuevo personaje
     * @param personaje Objeto Personaje con los datos a guardar
     * @return Número de filas afectadas (1 si se creó correctamente)
     */
    public int crearPersonaje(Personaje personaje) {
        return personajeRepository.create(personaje);
    }
    
    /**
     * Obtiene un personaje por su ID
     * @param id ID del personaje
     * @return Optional con el personaje si existe
     */
    public Optional<Personaje> obtenerPersonaje(Long id) {
        return personajeRepository.findById(id);
    }
    
    /**
     * Obtiene todos los personajes
     * @return Lista de todos los personajes
     */
    public List<Personaje> obtenerTodosLosPersonajes() {
        return personajeRepository.findAll();
    }
    
    /**
     * Actualiza un personaje existente
     * @param personaje Objeto Personaje con los datos actualizados
     * @return Número de filas afectadas
     */
    public int actualizarPersonaje(Personaje personaje) {
        if (!personajeRepository.findById(personaje.getId_personaje()).isPresent()) {
            throw new IllegalArgumentException("Personaje no encontrado");
        }
        return personajeRepository.update(personaje);
    }
    
    /**
     * Elimina un personaje por su ID
     * @param id ID del personaje a eliminar
     * @return Número de filas afectadas
     */
    public int eliminarPersonaje(Long id) {
        return personajeRepository.deleteById(id);
    }
    

    // METODOS ESPECIFICOS
    /**
     * Obtiene todos los personajes de un clan específico
     * @param clanId ID del clan
     * @return Lista de personajes del clan
     */
    public List<Personaje> obtenerPersonajesPorClan(Long clanId) {
        return personajeRepository.findAllByClanId(clanId);
    }
    
    /**
     * Obtiene personajes por clase (útil para armar grupos)
     * @param clase Clase del personaje (Guerrero, Mago, etc.)
     * @return Lista de personajes de esa clase
     */
    public List<Personaje> obtenerPorClase(String clase) {
        return personajeRepository.findByClase(clase);
    }
    
    /**
     * Obtiene personajes por rol de clan
     * @param rolClan Rol del personaje en el clan (Raider, Tanque, etc.)
     * @return Lista de personajes con ese rol
     */
    public List<Personaje> obtenerPorRolClan(String rolClan) {
        return personajeRepository.findByRolClan(rolClan);
    }
    
    /**
     * Obtiene personajes con item_level mínimo
     * @param itemLevel Item level mínimo requerido
     * @return Lista de personajes que cumplen el requisito
     */
    public List<Personaje> obtenerPorItemLevelMin(Integer itemLevel) {
        return personajeRepository.findByItemLevelMin(itemLevel);
    }
    
    /**
     * Actualiza los puntos de mérito (DKP) de un personaje
     * @param idPersonaje ID del personaje
     * @param cantidad Cantidad a restar (positivo) o sumar (negativo)
     * @return Número de filas afectadas
     */
    public int actualizarPuntosMerito(Long idPersonaje, Integer cantidad) {
        return personajeRepository.updatePuntosMerito(idPersonaje, cantidad);
    }
    
    /**
     * Obtiene el personaje principal de un jugador
     * @param jugadorId ID del jugador
     * @return Optional con el personaje si existe
     */
    public Optional<Personaje> obtenerPorJugadorId(Long jugadorId) {
        return personajeRepository.findByJugadorId(jugadorId);
    }
    public List<Personaje> obtenerTodosPorJugadorId(Long jugadorId) {
        return personajeRepository.findAllByJugadorIdList(jugadorId);
    }

}