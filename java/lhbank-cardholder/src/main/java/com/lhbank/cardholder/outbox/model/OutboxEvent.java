package com.lhbank.cardholder.outbox.model;

/**
 * POJO for holding the OutboxEvent to be published.
 *
 */
public class OutboxEvent {

    private String aggregateId;
    private String eventType;
    private String eventKey;
    private byte[] payloadAvro;

    public OutboxEvent(String aggregateId, String eventType, String eventKey, byte[] payloadAvro) {
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.eventKey = eventKey;
        this.payloadAvro = payloadAvro;
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
}
