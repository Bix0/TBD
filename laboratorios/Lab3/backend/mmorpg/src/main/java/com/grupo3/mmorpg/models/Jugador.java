package com.grupo3.mmorpg.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entidad que representa un Jugador en el sistema MMORPG
 * Mapea a la colección: jugadores
 */
@Document(collection = "jugadores")
@Data
@NoArgsConstructor
@AllArgsConstructor
// Agregamos un índice compuesto para consultas frecuentes de usuarios filtrados por rol
@CompoundIndexes({
        @CompoundIndex(name = "username_rol_idx", def = "{'username': 1, 'rol': 1}")
})
public class Jugador {

    @Id
    private String idJugador;

    // Índice único: Garantiza la restricción de integridad (no pueden haber dos usernames iguales)
    // y optimiza drásticamente las búsquedas al momento de iniciar sesión (JWT).
    @Indexed(unique = true)
    private String username;

    @ToString.Exclude
    private String password;

    // Indexado individualmente para reportes rápidos de cuántos Admins o Usuarios hay
    @Indexed
    private String rol;
}