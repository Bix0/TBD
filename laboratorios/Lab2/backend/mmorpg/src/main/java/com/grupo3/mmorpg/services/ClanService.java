package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.Clan;
import com.grupo3.mmorpg.repositories.ClanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para operaciones de negocio relacionadas con Clanes
 */
@Service
public class ClanService {

    private final ClanRepository clanRepository;

    public ClanService(ClanRepository clanRepository) {
        this.clanRepository = clanRepository;
    }

    @Transactional
    public Clan crearClan(Clan clan) {
        if (clanRepository.existsByNombre(clan.getNombre())) {
            throw new IllegalArgumentException("El nombre del clan ya existe");
        }
        return clanRepository.save(clan);
    }

    public Optional<Clan> obtenerClan(Long id) {
        return clanRepository.findById(id);
    }

    public List<Clan> obtenerTodosLosClanes() {
        return clanRepository.findAll();
    }

    @Transactional
    public Clan actualizarClan(Clan clan) {
        if (!clanRepository.findById(clan.getIdClan()).isPresent()) {
            throw new IllegalArgumentException("Clan no encontrado");
        }
        return clanRepository.save(clan);
    }

    @Transactional
    public int cambiarLider(Long idClan, Long nuevoLider) {
        if (!clanRepository.findById(idClan).isPresent()) {
            throw new IllegalArgumentException("Clan no encontrado");
        }
        return clanRepository.updateLider(idClan, nuevoLider);
    }

    @Transactional
    public void eliminarClan(Long id) {
        clanRepository.deleteById(id);
    }

    public Optional<Clan> buscarPorNombre(String nombre) {
        return clanRepository.findByNombre(nombre);
    }

    public boolean existeNombreClan(String nombre) {
        return clanRepository.existsByNombre(nombre);
    }

    public Optional<Long> obtenerLiderId(Long idClan) {
        return clanRepository.findIdLiderByIdClan(idClan);
    }

    public List<Object[]> obtenerAuditoriaLiderazgo() {
        return clanRepository.obtenerAuditoriaLiderazgo();
    }
}
