package com.lhbank.cardholder.outbox.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity that maps the Eventing OUTBOX table.
 *
 */
@Entity
@Table(name = "outbox")
public class OutboxDO {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "aggregate_id")
    private String aggregateId;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "event_key")
    private String eventKey;

    @Column(name="payload_avro")
    private byte[] payloadAvro;

    @Column(name = "createdAt")
    private Instant createdAt;

    @Column(name = "createdAtEpoch")
    private long createdAtEpoch;

    public OutboxDO() {
    }

    public OutboxDO(UUID id, String aggregateId, String eventType, String eventKey, byte[] payloadAvro, Instant createdAt) {
        this.id = id;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.eventKey = eventKey;
        this.payloadAvro = payloadAvro;
        this.createdAt = createdAt;
        this.createdAtEpoch = createdAt.toEpochMilli();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public byte[] getPayloadAvro() {
        return payloadAvro;
    }

    public void setPayloadAvro(byte[] payloadAvro) {
        this.payloadAvro = payloadAvro;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCreatedAtEpoch() {
        return createdAtEpoch;
    }

}
