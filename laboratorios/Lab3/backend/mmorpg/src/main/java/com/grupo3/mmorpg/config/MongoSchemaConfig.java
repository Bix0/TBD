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

    /**
     * Aplica la validación de esquema ($jsonSchema) de MongoDB a todas las colecciones del sistema.
     * Garantiza tipos de datos BSON, campos obligatorios, rangos válidos y reglas de negocio
     * reemplazando la rigidez que existía en PostgreSQL (Lab2).
     */
    @PostConstruct
    public void aplicarValidacionesEsquemasGlobales() {
        // 1. Colección: historial_loot
        Document historialLootSchema = new Document("bsonType", "object")
                .append("required", List.of("raidId", "personajeId", "itemId", "participoRaid", "estadoPersonaje"))
                .append("properties", new Document()
                        .append("raidId", new Document("bsonType", "string"))
                        .append("personajeId", new Document("bsonType", "string"))
                        .append("itemId", new Document("bsonType", "string"))
                        .append("participoRaid", new Document("enum", List.of(true)))
                        .append("estadoPersonaje", new Document("enum", List.of("Activo", "Vivo")))
                );
        aplicarValidacionColeccion("historial_loot", historialLootSchema);

        // 2. Colección: jugadores
        Document jugadoresSchema = new Document("bsonType", "object")
                .append("required", List.of("username", "password", "rol"))
                .append("properties", new Document()
                        .append("username", new Document("bsonType", "string"))
                        .append("password", new Document("bsonType", "string"))
                        .append("rol", new Document("enum", List.of("ADMIN", "USUARIO", "Admin", "Usuario", "ROLE_ADMIN", "ROLE_USER")))
                );
        aplicarValidacionColeccion("jugadores", jugadoresSchema);

        // 3. Colección: personajes
        Document personajesSchema = new Document("bsonType", "object")
                .append("required", List.of("jugadorId", "nombre", "clase", "nivel", "faccion", "itemLevel", "estado"))
                .append("properties", new Document()
                        .append("jugadorId", new Document("bsonType", "string"))
                        .append("nombre", new Document("bsonType", "string"))
                        .append("clase", new Document("bsonType", "string"))
                        .append("nivel", new Document("bsonType", List.of("int", "long", "double")).append("minimum", 1).append("maximum", 100))
                        .append("faccion", new Document("enum", List.of("Alianza", "Horda")))
                        .append("itemLevel", new Document("bsonType", List.of("int", "long", "double")).append("minimum", 0))
                        .append("puntosMerito", new Document("bsonType", List.of("int", "long", "double")).append("minimum", 0))
                        .append("estado", new Document("enum", List.of("Activo", "Caido", "Inactivo")))
                );
        aplicarValidacionColeccion("personajes", personajesSchema);

        // 4. Colección: clanes
        Document clanesSchema = new Document("bsonType", "object")
                .append("required", List.of("nombre", "faccion"))
                .append("properties", new Document()
                        .append("nombre", new Document("bsonType", "string"))
                        .append("faccion", new Document("enum", List.of("Alianza", "Horda")))
                );
        aplicarValidacionColeccion("clanes", clanesSchema);

        // 5. Colección: raids
        Document raidsSchema = new Document("bsonType", "object")
                .append("required", List.of("nombre", "estado", "itemLevelRequerido"))
                .append("properties", new Document()
                        .append("nombre", new Document("bsonType", "string"))
                        .append("estado", new Document("enum", List.of("Programada", "En curso", "Finalizada", "Completada", "Abierta")))
                        .append("itemLevelRequerido", new Document("bsonType", List.of("int", "long", "double")).append("minimum", 0))
                        .append("cuposTanque", new Document("bsonType", List.of("int", "long", "double")).append("minimum", 0))
                        .append("cuposHealer", new Document("bsonType", List.of("int", "long", "double")).append("minimum", 0))
                        .append("cuposDps", new Document("bsonType", List.of("int", "long", "double")).append("minimum", 0))
                        .append("tiempoFinalizacionMinutos", new Document("bsonType", List.of("int", "long", "double")).append("minimum", 0))
                );
        aplicarValidacionColeccion("raids", raidsSchema);

        // 6. Colección: items
        Document itemsSchema = new Document("bsonType", "object")
                .append("required", List.of("nombre", "itemLvl"))
                .append("properties", new Document()
                        .append("nombre", new Document("bsonType", "string"))
                        .append("itemLvl", new Document("bsonType", List.of("int", "long", "double")).append("minimum", 1))
                        .append("gananciaDkp", new Document("bsonType", List.of("int", "long", "double")).append("minimum", 0))
                        .append("clasesPermitidas", new Document("bsonType", "array").append("items", new Document("bsonType", "string")))
                );
        aplicarValidacionColeccion("items", itemsSchema);

        // 7. Colección: inscripciones_raid
        Document inscripcionesSchema = new Document("bsonType", "object")
                .append("required", List.of("raidId", "personajeId"))
                .append("properties", new Document()
                        .append("raidId", new Document("bsonType", "string"))
                        .append("personajeId", new Document("bsonType", "string"))
                        .append("asistio", new Document("bsonType", "bool"))
                        .append("danoTotal", new Document("bsonType", List.of("int", "long", "double")).append("minimum", 0))
                );
        aplicarValidacionColeccion("inscripciones_raid", inscripcionesSchema);

        // 8. Colección: inventarios
        Document inventariosSchema = new Document("bsonType", "object")
                .append("required", List.of("itemId", "personajeId"))
                .append("properties", new Document()
                        .append("itemId", new Document("bsonType", "string"))
                        .append("personajeId", new Document("bsonType", "string"))
                        .append("cantidad", new Document("bsonType", List.of("int", "long", "double")).append("minimum", 1))
                        .append("equipado", new Document("bsonType", "bool"))
                );
        aplicarValidacionColeccion("inventarios", inventariosSchema);

        // 9. Colección: auditoria_liderazgo
        Document auditoriaSchema = new Document("bsonType", "object")
                .append("required", List.of("clanId", "nuevoLiderId"))
                .append("properties", new Document()
                        .append("clanId", new Document("bsonType", "string"))
                        .append("nuevoLiderId", new Document("bsonType", "string"))
                );
        aplicarValidacionColeccion("auditoria_liderazgo", auditoriaSchema);
    }

    /**
     * Aplica el validador $jsonSchema con nivel estricto a una colección de MongoDB.
     */
    private void aplicarValidacionColeccion(String coleccion, Document validator) {
        Document jsonSchema = new Document("$jsonSchema", validator);
        try {
            if (mongoTemplate.collectionExists(coleccion)) {
                Document command = new Document("collMod", coleccion)
                        .append("validator", jsonSchema)
                        .append("validationLevel", "strict")
                        .append("validationAction", "error");
                mongoTemplate.executeCommand(command);
            } else {
                Document createCommand = new Document("create", coleccion)
                        .append("validator", jsonSchema)
                        .append("validationLevel", "strict")
                        .append("validationAction", "error");
                mongoTemplate.executeCommand(createCommand);
            }
            System.out.println("✅ Esquema $jsonSchema aplicado correctamente a la colección: " + coleccion);
        } catch (Exception e) {
            System.err.println("⚠️ Aviso al aplicar $jsonSchema a '" + coleccion + "': " + e.getMessage());
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