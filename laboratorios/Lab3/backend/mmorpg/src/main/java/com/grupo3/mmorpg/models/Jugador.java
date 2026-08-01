package com.grupo3.mmorpg.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
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
public class Jugador {

    @Id
    private String idJugador;

    @Indexed(unique = true)
    private String username;

    @ToString.Exclude
    private String password;

    private String rol;
}