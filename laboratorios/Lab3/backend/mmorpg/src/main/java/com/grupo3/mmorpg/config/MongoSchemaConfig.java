package com.grupo3.mmorpg.config;

import org.bson.Document;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Configuration
public class MongoSchemaConfig {

    private final MongoTemplate mongoTemplate;

    public MongoSchemaConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void aplicarValidacionHistorialLoot() {
        String coleccion = "historial_loot";

        // Construcción directa del esquema BSON $jsonSchema para MongoDB
        Document jsonSchema = new Document("$jsonSchema", new Document()
                .append("bsonType", "object")
                .append("required", List.of("raidId", "personajeId", "itemId", "participoRaid", "estadoPersonaje"))
                .append("properties", new Document()
                        .append("raidId", new Document("bsonType", "string"))
                        .append("personajeId", new Document("bsonType", "string"))
                        .append("itemId", new Document("bsonType", "string"))
                        // Exige que participoRaid sea boolean y su valor sea obligatoriamente true
                        .append("participoRaid", new Document("enum", List.of(true)))
                        // Exige que estadoPersonaje solo sea "Activo" o "Vivo" (rechaza "Caido")
                        .append("estadoPersonaje", new Document("enum", List.of("Activo", "Vivo")))
                )
        );

        // Si la colección existe, actualizamos su esquema de validación
        if (mongoTemplate.collectionExists(coleccion)) {
            Document command = new Document("collMod", coleccion)
                    .append("validator", jsonSchema)
                    .append("validationLevel", "strict")
                    .append("validationAction", "error");
            mongoTemplate.executeCommand(command);
        } else {
            // Si no existe, creamos la colección aplicando el validador directo
            Document createCommand = new Document("create", coleccion)
                    .append("validator", jsonSchema)
                    .append("validationLevel", "strict")
                    .append("validationAction", "error");
            mongoTemplate.executeCommand(createCommand);
        }
    }

    /**
     * Índices adicionales requeridos por el enunciado (Lab3, tarea 5):
     * - TTL: los eventos de raid (raid_events) expiran solos tras 1 hora.
     * - Texto: búsqueda por contenido en ítems (clases permitidas).
     */
    @PostConstruct
    public void crearIndicesAdicionales() {
        // Índice TTL sobre raid_events.timestamp: borrado automático de eventos viejos
        try {
            mongoTemplate
                    .indexOps("raid_events")
                    .ensureIndex(new Index().on("timestamp", Sort.Direction.ASC).expire(3600));
            System.out.println("✅ Índice TTL creado: raid_events.timestamp (expira en 3600s).");
        } catch (Exception e) {
            System.err.println("Aviso: no se pudo crear el índice TTL en raid_events: " + e.getMessage());
        }

        // Índice de texto sobre items.clasesPermitidas: búsqueda por contenido
        try {
            mongoTemplate
                    .indexOps("items")
                    .ensureIndex(new TextIndexDefinition.TextIndexDefinitionBuilder()
                            .onField("clasesPermitidas")
                            .build());
            System.out.println("✅ Índice de texto creado: items.clasesPermitidas.");
        } catch (Exception e) {
            System.err.println("Aviso: no se pudo crear el índice de texto en items: " + e.getMessage());
        }
    }
}