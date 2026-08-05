package com.grupo3.mmorpg.listeners;

import com.grupo3.mmorpg.services.LootService;
import com.grupo3.mmorpg.services.RankingService;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.messaging.Message;
import org.springframework.data.mongodb.core.messaging.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class RaidBossListener implements MessageListener<ChangeStreamDocument<Document>, Document> {

    private final LootService lootService;
    private final RankingService rankingService;

    @Autowired
    public RaidBossListener(LootService lootService, RankingService rankingService) {
        this.lootService = lootService;
        this.rankingService = rankingService;
    }

    @Override
    public void onMessage(Message<ChangeStreamDocument<Document>, Document> message) {
        Document eventDoc = message.getBody();
        if (eventDoc == null) {
            return;
        }

        String raidId = extractString(eventDoc, "raidId");
        String clanId = extractString(eventDoc, "clanId");
        String idItem = extractString(eventDoc, "idItem");
        String idPersonaje = extractString(eventDoc, "idPersonaje");
        String raidName = eventDoc.getString("raidName");
        boolean isAlreadyDistributed = Boolean.TRUE.equals(eventDoc.getBoolean("isAlreadyDistributed"));

        if (raidId == null) {
            System.err.println("Evento recibido sin raidId válido: " + eventDoc.toJson());
            return;
        }

        System.out.println("🔥 [BOSS DERROTADO] Evento en Raid: " + (raidName != null ? raidName : raidId) + " (ID: " + raidId + ")");

        try {
            if (!isAlreadyDistributed) {
                // 1. Ejecutar distribución automática de Loot si no se ha distribuido de forma síncrona previa
                lootService.distributeBossLoot(raidId, clanId, idItem, idPersonaje);
                System.out.println("✅ Distribución de Loot procesada por ChangeStream.");
            } else {
                System.out.println("ℹ️ Botín asignado previamente de forma síncrona. Se omite duplicidad.");
            }

            // 2. Actualizar colección materializada del Ranking de Clanes en MongoDB
            rankingService.actualizarRankingMaterializadoClanes();
            System.out.println("✅ Ranking de clanes actualizado en MongoDB.");

        } catch (Exception e) {
            System.err.println("Aviso procesando evento de muerte del Boss: " + e.getMessage());
        }
    }

    private String extractString(Document doc, String field) {
        Object val = doc.get(field);
        if (val == null) return null;
        return val.toString();
    }
}
