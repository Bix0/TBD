package com.grupo3.mmorpg.controllers;

import com.grupo3.mmorpg.services.RankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller REST para operaciones con Ranking
 * Endpoints: /api/ranking
 */
@RestController
@RequestMapping("/api/ranking")
public class RankingController {
    
    private final RankingService rankingService;
    
    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    // METODOS PARA RANKING
    /**
     * Obtiene el ranking actual de clanes
     * GET /api/ranking
     * @return Lista de mapas con los datos del ranking
     */
    @GetMapping
    public List<Map<String, Object>> obtenerRanking() {
        return rankingService.obtenerRanking();
    }
    
    /**
     * Actualiza la vista materializada del ranking
     * POST /api/ranking/refresh
     * @return true si se actualizó correctamente
     */
    @PostMapping("/refresh")
    public ResponseEntity<Boolean> actualizarRanking() {
        return ResponseEntity.ok(rankingService.actualizarRanking());
    }
    
    /**
     * Obtiene el ranking con un límite de resultados
     * GET /api/ranking/limite/{limite}
     * @param limite Número máximo de resultados
     * @return Lista de mapas con los datos del ranking
     */
    @GetMapping("/limite/{limite}")
    public List<Map<String, Object>> obtenerRankingConLimite(@PathVariable Integer limite) {
        return rankingService.obtenerRankingConLimite(limite);
    }
    
    /**
     * Obtiene el top N personajes por asistencia
     * GET /api/ranking/top/{top}
     * @param top Número de personajes a obtener
     * @return Lista de mapas con los datos del ranking
     */
    @GetMapping("/top/{top}")
    public List<Map<String, Object>> obtenerTopPersonajes(@PathVariable Integer top) {
        return rankingService.obtenerTopPersonajes(top);
    }
    
    /**
     * Obtiene el ranking filtrado por clase
     * GET /api/ranking/clase/{clase}
     * @param clase Clase a filtrar
     * @return Lista de mapas con los datos del ranking
     */
    @GetMapping("/clase/{clase}")
    public List<Map<String, Object>> obtenerRankingPorClase(@PathVariable String clase) {
        return rankingService.obtenerRankingPorClase(clase);
    }
    
    /**
     * Obtiene el ranking filtrado por DKP mínimo
     * GET /api/ranking/dkp/{dkpMinimo}
     * @param dkpMinimo DKP mínimo
     * @return Lista de mapas con los datos del ranking
     */
    @GetMapping("/dkp/{dkpMinimo}")
    public List<Map<String, Object>> obtenerRankingPorDkpMin(@PathVariable Integer dkpMinimo) {
        return rankingService.obtenerRankingPorDkpMin(dkpMinimo);
    }
    
    /**
     * Obtiene estadísticas del ranking
     * GET /api/ranking/estadisticas
     * @return Mapa con las estadísticas
     */
    @GetMapping("/estadisticas")
    public Map<String, Object> obtenerEstadisticasRanking() {
        return rankingService.obtenerEstadisticasRanking();
    }

    /**
     * Requerimiento 4: Obtiene el ranking de clanes basado en su desempeño en raids
     * GET /api/ranking/clanes-desempeno
     * @return Lista de mapas con los datos del ranking (daño total, asistencia, tiempo promedio)
     */
    @GetMapping("/clanes-desempeno")
    public List<Map> obtenerRankingClanesPorDesempeno() {
        return rankingService.obtenerRankingClanesPorDesempeno();
    }
}