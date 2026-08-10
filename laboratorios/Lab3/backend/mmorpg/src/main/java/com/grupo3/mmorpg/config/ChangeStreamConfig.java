package com.grupo3.mmorpg.config;

import com.grupo3.mmorpg.listeners.RaidBossListener;
import org.bson.Document;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.messaging.ChangeStreamRequest;
import org.springframework.data.mongodb.core.messaging.DefaultMessageListenerContainer;
import org.springframework.data.mongodb.core.messaging.MessageListenerContainer;
import org.springframework.data.mongodb.core.query.Criteria;

@Configuration
public class ChangeStreamConfig {

    @Bean
    public MessageListenerContainer changeStreamContainer(
            MongoTemplate mongoTemplate,
            RaidBossListener listener) {

        MessageListenerContainer container = new DefaultMessageListenerContainer(mongoTemplate);
        container.start();

        ChangeStreamRequest<Document> request = ChangeStreamRequest.builder(listener)
                .collection("raid_events")
                .filter(Aggregation.newAggregation(
                        Aggregation.match(Criteria.where("operationType").is("insert")),
                        Aggregation.match(Criteria.where("fullDocument.eventType").is("BOSS_DEATH"))
                ))
                .build();

        container.register(request, Document.class);
        return container;
    }
}
