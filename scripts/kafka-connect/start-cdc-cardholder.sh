#!/bin/bash

curl -X PUT \
  "http://$DATAPLATFORM_IP:8083/connectors/customer.dbzsrc.log-based-cdc/config" \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
  "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
  "tasks.max": "1",
  "slot.name":"dbzlogbased2",
  "database.server.name": "postgresql",
  "database.port": "5432",
  "database.user": "customer",
  "database.password": "abc123!",  
  "database.dbname": "customer_db",
  "schema.include.list": "public",
  "table.include.list": "public.person, public.address, public.card, public.country",
  "plugin.name": "pgoutput",
  "topic.prefix": "customer",  
  "tombstones.on.delete": "false",
  "database.hostname": "postgresql",
  "transforms":"unwrap,dropPrefix",
  "transforms.unwrap.type": "io.debezium.transforms.ExtractNewRecordState",
  "transforms.dropPrefix.type": "org.apache.kafka.connect.transforms.RegexRouter",  
  "transforms.dropPrefix.regex": "customer.public.(.*)",  
  "transforms.dropPrefix.replacement": "priv.$1.dbz.v1",
  "topic.creation.default.replication.factor": 3,
  "topic.creation.default.partitions": 2,
  "topic.creation.default.cleanup.policy": "compact"
}'