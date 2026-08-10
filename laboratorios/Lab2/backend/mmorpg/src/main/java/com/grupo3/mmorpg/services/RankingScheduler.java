package com.grupo3.mmorpg.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RankingScheduler {

    private final RankingService rankingService;

    public RankingScheduler(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    // Se ejecuta automáticamente cada 3 minutos
    //vizualiza ranking actualizado
    @Scheduled(fixedRate = 180000)
    public void refrescarVistasMaterializadas() {
        try {
            rankingService.actualizarRanking();
            System.out.println("🔄 [Scheduler] Vistas materializadas de DKP y Mapa de Calor actualizadas en BD.");
        } catch (Exception e) {
            System.err.println(" Error refrescando vistas materializadas: " + e.getMessage());
        }
    }
}