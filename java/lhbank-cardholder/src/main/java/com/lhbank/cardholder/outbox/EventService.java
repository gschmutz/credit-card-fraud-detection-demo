package com.lhbank.cardholder.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.lhbank.cardholder.outbox.model.OutboxDO;
import com.lhbank.cardholder.outbox.model.OutboxEvent;
import com.lhbank.cardholder.outbox.repository.OutboxRepository;

import java.time.Instant;
import java.util.UUID;

/**
 * Event Service responsible for persisting the event in the database.
 *
 */
@Service
public class EventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);

    /**
     * Handle to the Data Access Layer.
     */
    private final OutboxRepository outboxRepository;

    /**
     * Autowired constructor.
     *
     * @param outboxRepository
     */
    @Autowired
    public EventService(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    /**
     * This method handles all the events fired by the 'EventPublisher'. The method listens to events
     * and persists them in the database.
     *
     * @param event
     */
    @EventListener
    public void handleOutboxEvent(OutboxEvent event) {

        UUID uuid = UUID.randomUUID();
        OutboxDO entity = new OutboxDO(
                uuid,
                event.getAggregateId(),
                event.getEventType(),
                event.getEventKey() != null ? event.getEventKey() : event.getAggregateId(),
                event.getPayloadAvro(),
                Instant.now()
        );

        LOGGER.info("Handling event : {}.", entity);

        outboxRepository.save(entity);

        /*
         * Delete the event once written, so that the outbox doesn't grow.
         * The CDC eventing polls the database log entry and not the table in the database.
         */
        //outboxRepository.delete(entity);
    }
}
