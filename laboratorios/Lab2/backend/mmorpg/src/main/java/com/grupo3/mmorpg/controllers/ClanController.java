package com.grupo3.mmorpg.controllers;

import com.grupo3.mmorpg.models.Clan;
import com.grupo3.mmorpg.models.Personaje;
import com.grupo3.mmorpg.services.ClanService;
import com.grupo3.mmorpg.repositories.ClanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para operaciones con Clanes
 * Endpoints: /api/clanes
 */
@RestController
@RequestMapping("/api/clanes")
public class ClanController {

    private final ClanService clanService;

    // Inyectamos el repositorio solo para la consulta geoespacial rápida
    @Autowired
    private ClanRepository clanRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ClanController(ClanService clanService) {
        this.clanService = clanService;
    }

    /**
     * Crea un nuevo clan
     * POST /api/clanes
     * 
     * @param clan Objeto Clan con los datos
     * @return ResponseEntity con el clan creado
     */
    @PostMapping
    public ResponseEntity<Clan> crearClan(@RequestBody Clan clan) {
        try {
            clanService.crearClan(clan);
            return ResponseEntity.status(HttpStatus.CREATED).body(clan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Obtiene todos los clanes
     * GET /api/clanes
     * 
     * @return Lista de clanes
     */
    @GetMapping
    public List<Clan> obtenerTodosLosClanes() {
        return clanService.obtenerTodosLosClanes();
    }

    /**
     * Obtiene un clan por su ID
     * GET /api/clanes/{id}
     * 
     * @param id ID del clan
     * @return ResponseEntity con el clan o 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<Clan> obtenerClan(@PathVariable Long id) {
        return clanService.obtenerClan(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualiza un clan existente
     * PUT /api/clanes/{id}
     * 
     * @param id   ID del clan
     * @param clan Objeto Clan con los datos actualizados
     * @return ResponseEntity con el clan actualizado o 404
     */
    @PutMapping("/{id}")
    public ResponseEntity<Clan> actualizarClan(@PathVariable Long id, @RequestBody Clan clan) {
        clan.setIdClan(id);
        try {
            clanService.actualizarClan(clan);
            return ResponseEntity.ok(clan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Cambia el líder de un clan
     * PUT /api/clanes/{id}/lider
     * 
     * @param id         ID del clan
     * @param nuevoLider ID del nuevo líder
     * @return ResponseEntity con status 200 o 404
     *         Este endpoint activa el trigger trg_auditar_lider
     */
    @PutMapping("/{id}/lider")
    public ResponseEntity<Void> cambiarLider(@PathVariable Long id, @RequestParam Long nuevoLider) {
        try {
            clanService.cambiarLider(id, nuevoLider);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Elimina un clan por su ID
     * DELETE /api/clanes/{id}
     * 
     * @param id ID del clan
     * @return ResponseEntity con status 204 o 404
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarClan(@PathVariable Long id) {
        try {
            clanService.eliminarClan(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // METODOS ESPECIFICOS
    /**
     * Busca un clan por nombre
     * GET /api/clanes/nombre/{nombre}
     * 
     * @param nombre Nombre del clan
     * @return ResponseEntity con el clan o 404
     */
    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<Clan> buscarPorNombre(@PathVariable String nombre) {
        return clanService.buscarPorNombre(nombre)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Verifica si un nombre de clan ya existe
     * GET /api/clanes/exists/{nombre}
     * 
     * @param nombre Nombre del clan
     * @return true si existe, false en caso contrario
     */
    @GetMapping("/exists/{nombre}")
    public ResponseEntity<Boolean> existeNombreClan(@PathVariable String nombre) {
        return ResponseEntity.ok(clanService.existeNombreClan(nombre));
    }

    @PostMapping("/unirse/{idClan}")
    public ResponseEntity<Void> unirseAlClan(@PathVariable Long idClan, @RequestBody Long personaje) {
        try {
            clanService.unirseAlClan(idClan, personaje);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/salir/{idClan}")
    public ResponseEntity<Void> salirDeClan(@PathVariable Long idClan, @RequestBody Long personaje) {
        try {
            clanService.salirDeClan(idClan, personaje);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene el ID del líder de un clan
     * GET /api/clanes/{id}/lider-id
     * 
     * @param id ID del clan
     * @return ResponseEntity con el ID del líder o 404
     */
    @GetMapping("/{id}/lider-id")
    public ResponseEntity<Long> obtenerLiderId(@PathVariable Long id) {
        return clanService.obtenerLiderId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/auditoria")
    public ResponseEntity<List<Object[]>> obtenerAuditoriaLiderazgo() {
        return ResponseEntity.ok(clanService.obtenerAuditoriaLiderazgo());
    }

    // --- NUEVO ENDPOINT GEOESPACIAL (LAB 2) ---
    @GetMapping("/cercanos")
    public ResponseEntity<List<Clan>> getClanesCercanos(
            @RequestParam double lon,
            @RequestParam double lat,
            @RequestParam(defaultValue = "5000") double distancia,
            @RequestParam String faccion) {

        List<Clan> clanesCercanos = clanRepository.findClanesCercanos(lon, lat, distancia, faccion);
        if (clanesCercanos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clanesCercanos);
    }

    // --- ENDPOINT MAPA DE CALOR (LAB 2) ---
    @GetMapping("/mapa-calor")
    public ResponseEntity<List<Object[]>> getMapaCalor() {
        try {
            jdbcTemplate.execute("REFRESH MATERIALIZED VIEW mv_calor_clanes");
        } catch (Exception ignored) {
        }
        return ResponseEntity.ok(clanRepository.obtenerMapaCalorClanes());
    }
}