package com.lhbank.cardholder.outbox.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
