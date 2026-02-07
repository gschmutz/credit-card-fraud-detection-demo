package com.lhbank.cardholder.eventproducer;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.lhbank.cardholder.avro.CardHolderState;
import com.lhbank.cardholder.entity.Person;
import com.lhbank.cardholder.outbox.EventPublisher;
import com.lhbank.cardholder.outbox.model.OutboxEvent;

import java.util.HashMap;
import java.util.Map;

@Component
public class CardHolderStateProducer {

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    @Value("${customer.topic.name}")
    private String kafkaTopic;

    /**
     * Handle to the Outbox Eventing framework.
     */
    @Autowired
    private EventPublisher eventPublisher;

    public void send(Person person) {

        SchemaRegistryClient schemaRegistryClient = new CachedSchemaRegistryClient(schemaRegistryUrl, 10);
        Map<String, Object> props = new HashMap<>();
        // send correct schemas to the registry, without "avro.java.string"
        props.put(KafkaAvroSerializerConfig.AVRO_REMOVE_JAVA_PROPS_CONFIG, true);
        props.put("schema.registry.url", schemaRegistryUrl);
        KafkaAvroSerializer ser = new KafkaAvroSerializer(schemaRegistryClient, props);

        CardHolderState cardHolderState = CardHolderConverter.convert(person);

        OutboxEvent outboxEvent = new OutboxEvent(
                person.getId(),
                "cardHolder",
                person.getId(),       // use customerId for the Kafka key
                ser.serialize(kafkaTopic, cardHolderState)
        );

        eventPublisher.fire(outboxEvent);
    }

}
