package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.Clan;
import com.grupo3.mmorpg.repositories.ClanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para operaciones de negocio relacionadas con Clanes
 * Utiliza ClanRepository para acceder a los datos
 */
@Service
public class ClanService {
    
    private final ClanRepository clanRepository;
    
    public ClanService(ClanRepository clanRepository) {
        this.clanRepository = clanRepository;
    }
    
    // ============================================================================
    // CRUD BÁSICOS
    // ============================================================================
    
    /**
     * Crea un nuevo clan
     * @param clan Objeto Clan con los datos a guardar
     * @return Número de filas afectadas (1 si se creó correctamente)
     */
    public int crearClan(Clan clan) {
        // Validación: verificar que el nombre no exista
        if (clanRepository.existsByName(clan.getNombre())) {
            throw new IllegalArgumentException("El nombre del clan ya existe");
        }
        return clanRepository.create(clan);
    }
    
    /**
     * Obtiene un clan por su ID
     * @param id ID del clan
     * @return Optional con el clan si existe
     */
    public Optional<Clan> obtenerClan(Long id) {
        return clanRepository.findById(id);
    }
    
    /**
     * Obtiene todos los clanes
     * @return Lista de todos los clanes
     */
    public List<Clan> obtenerTodosLosClanes() {
        return clanRepository.findAll();
    }
    
    /**
     * Actualiza un clan existente
     * @param clan Objeto Clan con los datos actualizados
     * @return Número de filas afectadas
     */
    public int actualizarClan(Clan clan) {
        if (!clanRepository.findById(clan.getId_clan()).isPresent()) {
            throw new IllegalArgumentException("Clan no encontrado");
        }
        return clanRepository.update(clan);
    }
    
    /**
     * Cambia el líder de un clan
     * @param idClan ID del clan
     * @param nuevoLider ID del nuevo líder
     * @return Número de filas afectadas
     * Este método activa el trigger trg_auditar_lider
     */
    public int cambiarLider(Long idClan, Long nuevoLider) {
        if (!clanRepository.findById(idClan).isPresent()) {
            throw new IllegalArgumentException("Clan no encontrado");
        }
        return clanRepository.updateLider(idClan, nuevoLider);
    }
    
    /**
     * Elimina un clan por su ID
     * @param id ID del clan a eliminar
     * @return Número de filas afectadas
     */
    public int eliminarClan(Long id) {
        return clanRepository.deleteById(id);
    }
    
    // ============================================================================
    // MÉTODOS ESPECÍFICOS
    // ============================================================================
    
    /**
     * Busca un clan por nombre
     * @param nombre Nombre del clan
     * @return Optional con el clan si existe
     */
    public Optional<Clan> buscarPorNombre(String nombre) {
        return clanRepository.findByName(nombre);
    }
    
    /**
     * Verifica si un nombre de clan ya existe
     * @param nombre Nombre del clan a verificar
     * @return true si el nombre existe, false en caso contrario
     */
    public boolean existeNombreClan(String nombre) {
        return clanRepository.existsByName(nombre);
    }
    
    /**
     * Obtiene el ID del líder de un clan
     * @param idClan ID del clan
     * @return Optional con el ID del líder si existe
     */
    public Optional<Long> obtenerLiderId(Long idClan) {
        return clanRepository.getLiderId(idClan);
    }

    /**
     * Une a un personaje al clan con validaciones.
     * @param idClan ID del clan
     * @param idPersonaje ID del personaje
     * @return String de ingreso al clan
     */
    @Transactional
    public String unirseAClan(Long idClan, Long idPersonaje) {
        if (clanRepository.personajeTieneClan(idPersonaje)) {
            return "El personaje ya pertenece a un clan.";
        }

        if (!clanRepository.findById(idClan).isPresent()) {
            return "El clan no existe.";
        }

        clanRepository.añadirMiembro(idClan, idPersonaje);
        return "Te has unido al clan exitosamente.";
    }

    /**
     * Expulsa o permite que un miembro salga del clan.
     * Si es el líder, debe dar error.
     * @param idClan ID del clan
     * @param idPersonaje ID del personaje
     * @return String de abandono del clan
     */
    @Transactional
    public String abandonarClan(Long idClan, Long idPersonaje) {
        Optional<Long> liderId = clanRepository.getLiderId(idClan);

        if (liderId.isPresent() && liderId.get().equals(idPersonaje)) {
            return "Error: El líder no puede abandonar el clan. Debe transferir el liderazgo primero.";
        }

        clanRepository.eliminarMiembro(idPersonaje);
        return "Has abandonado el clan.";
    }
}

