package com.grupo3.mmorpg.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

@Configuration
public class MongoConfig {

    // ¡Esto habilita la anotación @Transactional para MongoDB!
    @Bean
    MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }
}

//Solo ve a cualquier servicio donde modifiques dos cosas a la vez (por ejemplo, restar oro a un jugador y darle un ítem)
// y ponle la anotación @Transactional arriba del metodo.
//  Si algo falla a la mitad, MongoDB hará un rollback automático. ¡Requisito de transacciones aprobado!