package com.grupo3.mmorpg.config;

import org.bson.Document;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

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
}