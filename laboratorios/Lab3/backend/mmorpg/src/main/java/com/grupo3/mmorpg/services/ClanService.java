package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.Clan;
import com.grupo3.mmorpg.repositories.ClanRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para operaciones de negocio relacionadas con Clanes en MongoDB
 */
@Service
public class ClanService {

    private final ClanRepository clanRepository;

    @Autowired
    private PersonajeService personajeService;

    public ClanService(ClanRepository clanRepository) {
        this.clanRepository = clanRepository;
    }

    @Transactional
    public Optional<Clan> unirseAlClan(String idClan, String idPersonaje) {
        Clan thisClan = clanRepository.findById(idClan)
                .orElseThrow(() -> new IllegalArgumentException("Clan no encontrado"));
        personajeService.unirseAlClan(idPersonaje, thisClan);
        return Optional.of(thisClan);
    }

    @Transactional
    public Optional<Clan> salirDeClan(String idClan, String idPersonaje) {
        Clan thisClan = clanRepository.findById(idClan)
                .orElseThrow(() -> new IllegalArgumentException("Clan no encontrado"));
        personajeService.salirDeClan(idPersonaje);
        return Optional.of(thisClan);
    }

    @Transactional
    public Clan crearClan(Clan clan) {
        if (clanRepository.existsByNombre(clan.getNombre())) {
            throw new IllegalArgumentException("El nombre del clan ya existe");
        }
        return clanRepository.save(clan);
    }

    public Optional<Clan> obtenerClan(String id) {
        return clanRepository.findById(id);
    }

    public List<Clan> obtenerTodosLosClanes() {
        return clanRepository.findAll();
    }

    @Transactional
    public Clan actualizarClan(Clan clan) {
        if (!clanRepository.existsById(clan.getIdClan())) {
            throw new IllegalArgumentException("Clan no encontrado");
        }
        return clanRepository.save(clan);
    }

    @Transactional
    public int cambiarLider(String idClan, String nuevoLider) {
        Clan clan = clanRepository.findById(idClan)
                .orElseThrow(() -> new IllegalArgumentException("Clan no encontrado"));
        clan.setIdLider(nuevoLider);
        clanRepository.save(clan);
        return 1;
    }

    @Transactional
    public void eliminarClan(String id) {
        clanRepository.deleteById(id);
    }

    public Optional<Clan> buscarPorNombre(String nombre) {
        return clanRepository.findByNombre(nombre);
    }

    public boolean existeNombreClan(String nombre) {
        return clanRepository.existsByNombre(nombre);
    }

    public Optional<String> obtenerLiderId(String idClan) {
        return clanRepository.findIdLiderByIdClan(idClan);
    }

    public List<Object[]> obtenerAuditoriaLiderazgo() {
        return List.of(); // Estructura adaptada para control documental
    }
}