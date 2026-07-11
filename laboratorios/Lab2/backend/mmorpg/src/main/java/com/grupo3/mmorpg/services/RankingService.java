package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.repositories.RaidRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Servicio para operaciones relacionadas con el Ranking de clanes
 * Utiliza la vista materializada mv_ranking_clan
 */
@Service
public class RankingService {
    
    private final JdbcTemplate jdbcTemplate;
    private final RaidRepository raidRepository;
    
    public RankingService(JdbcTemplate jdbcTemplate, RaidRepository raidRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.raidRepository = raidRepository;
    }

    // METODOS PARA RANKING
    /**
     * Obtiene el ranking actual de clanes
     * SELECT * FROM mv_ranking_clan
     * @return Lista de mapas con los datos del ranking
     */
    public List<Map<String, Object>> obtenerRanking() {
        String sql = "SELECT * FROM mv_ranking_clan ORDER BY total_raids_asistidas DESC, dkp_actual DESC";
        return jdbcTemplate.queryForList(sql);
    }
    
    /**
     * Actualiza la vista materializada del ranking
     * REFRESH MATERIALIZED VIEW mv_ranking_clan
     * @return true si se actualizó correctamente
     */
    public boolean actualizarRanking() {
        String sql = "REFRESH MATERIALIZED VIEW mv_ranking_clan";
        jdbcTemplate.execute(sql);
        return true;
    }
    
    /**
     * Obtiene el ranking con un límite de resultados
     * SELECT * FROM mv_ranking_clan ORDER BY total_raids_asistidas DESC, dkp_actual DESC LIMIT ?
     * @param limite Número máximo de resultados
     * @return Lista de mapas con los datos del ranking
     */
    public List<Map<String, Object>> obtenerRankingConLimite(Integer limite) {
        String sql = "SELECT * FROM mv_ranking_clan ORDER BY total_raids_asistidas DESC, dkp_actual DESC LIMIT ?";
        return jdbcTemplate.queryForList(sql, new Object[]{limite});
    }
    
    /**
     * Obtiene el top N personajes por asistencia
     * @param top Número de personajes a obtener
     * @return Lista de mapas con los datos del ranking
     */
    public List<Map<String, Object>> obtenerTopPersonajes(Integer top) {
        String sql = "SELECT * FROM mv_ranking_clan ORDER BY total_raids_asistidas DESC LIMIT ?";
        return jdbcTemplate.queryForList(sql, new Object[]{top});
    }
    
    /**
     * Obtiene el ranking filtrado por clase
     * SELECT * FROM mv_ranking_clan WHERE clase = ? ORDER BY total_raids_asistidas DESC, dkp_actual DESC
     * @param clase Clase a filtrar
     * @return Lista de mapas con los datos del ranking
     */
    public List<Map<String, Object>> obtenerRankingPorClase(String clase) {
        String sql = "SELECT * FROM mv_ranking_clan WHERE clase = ? ORDER BY total_raids_asistidas DESC, dkp_actual DESC";
        return jdbcTemplate.queryForList(sql, new Object[]{clase});
    }
    
    /**
     * Obtiene el ranking filtrado por DKP mínimo
     * SELECT * FROM mv_ranking_clan WHERE dkp_actual >= ? ORDER BY total_raids_asistidas DESC, dkp_actual DESC
     * @param dkpMinimo DKP mínimo
     * @return Lista de mapas con los datos del ranking
     */
    public List<Map<String, Object>> obtenerRankingPorDkpMin(Integer dkpMinimo) {
        String sql = "SELECT * FROM mv_ranking_clan WHERE dkp_actual >= ? ORDER BY total_raids_asistidas DESC, dkp_actual DESC";
        return jdbcTemplate.queryForList(sql, new Object[]{dkpMinimo});
    }
    
    /**
     * Obtiene estadísticas del ranking
     * @return Mapa con las estadísticas
     */
    public Map<String, Object> obtenerEstadisticasRanking() {
        String sqlTotal = "SELECT COUNT(*) as total, SUM(total_raids_asistidas) as raids_totales, AVG(dkp_actual) as dkp_promedio FROM mv_ranking_clan";
        return jdbcTemplate.queryForList(sqlTotal).get(0);
    }
}