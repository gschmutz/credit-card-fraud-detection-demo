#!/bin/bash

curl -X PUT \
  "http://$DATAPLATFORM_IP:8083/connectors/refdata.dbzsrc.log-based-cdc/config" \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
  "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
  "tasks.max": "1",
  "slot.name":"dbzlogbased",
  "database.server.name": "postgresql",
  "database.port": "5432",
  "database.user": "postgres",
  "database.password": "abc123!",  
  "database.dbname": "postgres",
  "schema.include.list": "public",
  "table.include.list": "public.merchant",
  "plugin.name": "pgoutput",
  "topic.prefix": "ref",  
  "tombstones.on.delete": "false",
  "database.hostname": "postgresql",
  "key.converter": "org.apache.kafka.connect.storage.StringConverter",
  "transforms":"unwrap,extractKey,dropPrefix",
  "transforms.unwrap.type": "io.debezium.transforms.ExtractNewRecordState",
  "transforms.extractKey.type": "org.apache.kafka.connect.transforms.ExtractField$Key",
  "transforms.extractKey.field": "merchant_id",
  "transforms.dropPrefix.type": "org.apache.kafka.connect.transforms.RegexRouter",
  "transforms.dropPrefix.regex": "ref.public.(.*)",
  "transforms.dropPrefix.replacement": "pub.ref.$1.state.v1",
  "topic.creation.default.replication.factor": 3,
  "topic.creation.default.partitions": 2,
  "topic.creation.default.cleanup.policy": "compact"
}'