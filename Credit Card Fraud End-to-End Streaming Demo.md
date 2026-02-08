# Credit Card Fraud End-to-End Streaming Demo




## Start simulator

```
docker compose --profile test up -d
```


## KSQL transaction

``` bash
docker exec -it ksqldb-cli ksql http://ksqldb-server-1:8088
```



``` sql
CREATE STREAM IF NOT EXISTS pay_transaction_s 
  WITH (kafka_topic='pub.payment.transaction.delta.v1',
        value_format='AVRO');
```

```sql
SELECT * FROM pay_transaction_s EMIT CHANGES;
```

```sql
SELECT card_number, COUNT(*) AS nof 
FROM pay_transaction_s
GROUP BY card_number
EMIT CHANGES;
```

``` sql
SELECT card_number, COUNT(*) AS nof, SUM(amount) AS sum
FROM pay_transaction_s 
GROUP BY card_number
HAVING COUNT(*) > 1
EMIT CHANGES;
```


```sql
CREATE TABLE IF NOT EXISTS mer_blacklist_t (key VARCHAR PRIMARY KEY, merchant_id VARCHAR)
WITH (kafka_topic='pub.merchant.blacklist.state.v1',
        value_format='AVRO', key_format='AVRO');
```


```sql
SELECT t.*
	, CASE 
			WHEN bl.key IS NOT NULL 
			THEN 1 else 0 
		END 					AS is_flagged
FROM pay_transaction_s		t
LEFT JOIN mer_blacklist_t  bl
ON (t.merchant_id = bl.key)
EMIT CHANGES;
```

```
DROP STREAM IF EXISTS pay_transaction_flagged_s;

CREATE STREAM pay_transaction_flagged_s
  WITH (kafka_topic='priv.payment.transaction-flagged.delta.v1',
        value_format='AVRO', key_format='AVRO', partitions=2)
AS
	SELECT t.transaction_id
	, t.card_number
	, t.country
	, t.city
	, t.currency
	, t.amount
	, t.merchant_id			AS merchant_id
	, t.merchant_category_id
	, t.channel
	, t.transaction_date
	, CASE 
			WHEN bl.key IS NOT NULL 
			THEN 1 else 0 
		END 					AS is_flagged
	, 'blacklist' 			AS flagged_reason
	FROM pay_transaction_s		t
	LEFT JOIN mer_blacklist_t  bl
		ON (t.merchant_id = bl.key)
	PARTITION BY t.card_number
	EMIT CHANGES; 
```

```sql
SELECT * 
FROM pay_transaction_flagged_s 
WHERE is_flagged = 1 
EMIT CHANGES;
```

```sql
INSERT INTO mer_blacklist_t (key, merchant_id)
VALUES ('merchant-199', 'merchant-199');
```


```sql
CREATE TABLE IF NOT EXISTS mer_category_t (key BIGINT PRIMARY KEY)
  WITH (kafka_topic='pub.merchant.category.state.v1',
        value_format='AVRO', key_format='AVRO');
```

```sql
CREATE TABLE IF NOT EXISTS mer_merchant_t (key STRING PRIMARY KEY)
  WITH (kafka_topic='pub.merchant.merchant.state.v1',
        value_format='AVRO', key_format='AVRO');
```

```sql
SELECT t.*
	, mc.name
	, m.name
FROM pay_transaction_flagged_s		t
LEFT JOIN mer_category_t	mc
	ON (t.merchant_category_id = mc.key)
LEFT JOIN mer_merchant_t m
	ON (t.merchant_id = m.key)
EMIT CHANGES; 
```

```sql
DROP STREAM IF EXISTS pay_transaction_flagged_enriched_s;

CREATE STREAM pay_transaction_flagged_enriched_s
  WITH (kafka_topic='priv.payment.transaction-flagged-enriched.delta.v1',
        value_format='AVRO', key_format='AVRO', partitions=2)
AS
	SELECT t.transaction_id
		, t.card_number
		, t.country
		, t.city
		, t.currency
		, t.amount
		, t.merchant_id		AS merchant_id
		, t.merchant_category_id
		, t.channel
		, t.transaction_date
		, t.is_flagged
		, t.flagged_reason
		, mc.name				AS category_name
		, m.name 				AS merchant_name
	FROM pay_transaction_flagged_s		t
	LEFT JOIN mer_category_t	mc
	ON (t.merchant_category_id = mc.key)
	LEFT JOIN mer_merchant_t m
	ON (t.merchant_id = m.key)
	PARTITION BY t.card_number
	EMIT CHANGES; 
```

```sql
SELECT * 
FROM pay_transaction_flagged_enriched_s
WHERE is_flagged = 1 
EMIT CHANGES;
```


## Transactions to Iceberg

```
curl -X "GET" "$DOCKER_HOST_IP:8083/connector-plugins" | jq
```

```bash
docker exec -it kafka-1 kafka-topics --bootstrap-server kafka-1:19092 --create --topic control-iceberg --partitions 1 --replication-factor 3
```

### Raw Transactions to Iceberg


#### a) Spark SQL

```bash
docker exec -ti spark-master spark-sql
```

```sql
use hiverest;

CREATE DATABASE payment_db
LOCATION 's3a://iceberg-bucket/payment_db';

CREATE TABLE payment_db.transaction_t (
    transaction_id STRING,
    card_number STRING,
    country STRING,
    city STRING,
    currency STRING,
    amount DOUBLE,
    merchant_id STRING,
    merchant_category_id STRING,
    channel STRING,
    transaction_date TIMESTAMP)
PARTITIONED BY (hours(transaction_date));
```

#### b) Presto

```
CREATE SCHEMA payment_db
WITH (
  location = 's3a://iceberg-bucket/payment_db'
);

CREATE TABLE "iceberg_data"."payment_db"."transaction_t" (
    transaction_id VARCHAR,
    card_number VARCHAR,
    country VARCHAR,
    city VARCHAR,
    currency VARCHAR,
    amount DOUBLE,
    merchant_id VARCHAR,
    merchant_category_id VARCHAR,
    channel VARCHAR,
    transaction_date TIMESTAMP
)
WITH (
    partitioning = ARRAY['hour(transaction_date)']
);
```

Create the bucket

```bash
docker exec -ti minio-mc mc mb minio-1/iceberg-bucket
```

#### Kafka Connect (local)

```bash
#!/bin/bash

curl -X PUT \
  http://$DATAPLATFORM_IP:8083/connectors/pay-transaction-kafka-to-s3/config \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
      "connector.class": "org.apache.iceberg.connect.IcebergSinkConnector",
      "tasks.max": "1",
      "topics": "pub.payment.transaction.delta.v1",
      "iceberg.tables": "payment_db.transaction_t",
      "iceberg.tables.dynamic-enabled": "false",
      "write.upsert.enabled": "false",
      "iceberg.control.commit.interval-ms": "60000",
      "consumer.max.poll.records": "5000",
      "iceberg.catalog.type": "rest",
      "iceberg.catalog.uri": "http://hive-metastore:9084/iceberg",
      "iceberg.catalog.warehouse": "s3a://lakehouse-bucket/payment_db",      
      "iceberg.catalog.client.region": "us-east-1",
      "iceberg.catalog.s3.endpoint": "http://minio-1:9000",
      "iceberg.catalog.s3.path-style-access": "true",
      "iceberg.catalog.s3.access-key-id": "admin",
      "iceberg.catalog.s3.secret-access-key": "abc123abc123!",
      "value.converter": "io.confluent.connect.avro.AvroConverter",
      "value.converter.schema.registry.url": "http://schema-registry-1:8081",
      "key.converter": "org.apache.kafka.connect.storage.StringConverter"
	}'
```

#### Kafka Connect (Watson.x Data)

```
curl -X PUT \
  http://$DATAPLATFORM_IP:8083/connectors/IBM_pay-transaction-kafka-to-s3/config \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
      "connector.class": "org.apache.iceberg.connect.IcebergSinkConnector",
      "tasks.max": "1",
      "topics": "pub.payment.transaction.delta.v1",
      "iceberg.tables": "payment_db.transaction_t",
      "iceberg.tables.dynamic-enabled": "false",
      "write.upsert.enabled": "false",
      "iceberg.control.commit.interval-ms": "60000",
      "consumer.max.poll.records": "5000",
      "iceberg.catalog.type": "rest",
      "iceberg.catalog.uri": "https://ibm-lh-presto-svc:8180/mds/iceberg",
      "iceberg.catalog.warehouse": "iceberg_data",      
      "iceberg.catalog.client.region": "none",
      "iceberg.catalog.s3.endpoint": "http://ibm-lh-presto-svc:9000",
      "iceberg.catalog.s3.path-style-access": "true",
      "iceberg.catalog.s3.access-key-id": "f33150f834d9a8b2435474f6",
      "iceberg.catalog.s3.secret-access-key": "fdd7d613b2c72d07c3618ae6",
  "iceberg.catalog.rest.auth.type": "basic",
  "iceberg.catalog.rest.auth.basic.username": "admin",
  "iceberg.catalog.rest.auth.basic.password": "abc123abc123!",
      "value.converter": "io.confluent.connect.avro.AvroConverter",
      "value.converter.schema.registry.url": "http://schema-registry-1:8081",
      "key.converter": "org.apache.kafka.connect.storage.StringConverter"
	}'
```

### Flagged Transactions to Iceberg

```bash
docker exec -ti spark-master spark-sql
```

```sql
use hiverest;


CREATE TABLE payment_db.transaction_flagged_enriched_t (
    transactionId STRING,
    card_number STRING,
    country STRING,
    city STRING,
    currency STRING,
    amount DOUBLE,
    merchantId STRING,
    merchantCategoryId STRING,
    channel STRING,
    timestamp TIMESTAMP,
    flagged INTEGER,
    categoryName STRING,
    merchantName STRING)
PARTITIONED BY (hours(timestamp));
```

```bash
#!/bin/bash

curl -X PUT \
  http://$DATAPLATFORM_IP:8083/connectors/pay-transaction-flagged-enriched-kafka-to-s3/config \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
      "connector.class": "org.apache.iceberg.connect.IcebergSinkConnector",
      "tasks.max": "1",
      "topics": "priv.payment.transaction-flagged-enriched.delta.v1",
      "iceberg.tables": "payment_db.transaction_flagged_enriched_t",
      "iceberg.tables.dynamic-enabled": "false",
      "write.upsert.enabled": "false",
      "iceberg.control.commit.interval-ms": "60000",
      "consumer.max.poll.records": "5000",
      "iceberg.catalog.type": "rest",
      "iceberg.catalog.uri": "http://hive-metastore:9084/iceberg",
      "iceberg.catalog.warehouse": "s3a://lakehouse-bucket/payment_db",      
      "iceberg.catalog.client.region": "us-east-1",
      "iceberg.catalog.s3.endpoint": "http://minio-1:9000",
      "iceberg.catalog.s3.path-style-access": "true",
      "iceberg.catalog.s3.access-key-id": "admin",
      "iceberg.catalog.s3.secret-access-key": "abc123abc123!",
      "value.converter": "io.confluent.connect.avro.AvroConverter",
      "value.converter.schema.registry.url": "http://schema-registry-1:8081",
      "key.converter": "org.apache.kafka.connect.storage.StringConverter"
	}'
```


## Card Holder Integration

### Polling-based CDC using Kafka Connect

```bash
docker exec -ti kafka-1 kafka-topics --bootstrap-server kafka-1:19092 --create --topic priv.customer.person.cdc.v1 --partitions 2 --replication-factor 3

docker exec -ti kafka-1 kafka-topics --bootstrap-server kafka-1:19092 --create --topic priv.customer.address.cdc.v1 --partitions 2 --replication-factor 3

docker exec -ti kafka-1 kafka-topics --bootstrap-server kafka-1:19092 --create --topic priv.customer.country.cdc.v1 --partitions 2 --replication-factor 3

docker exec -ti kafka-1 kafka-topics --bootstrap-server kafka-1:19092 --create --topic priv.customer.card.cdc.v1 --partitions 2 --replication-factor 3
```

```bash
curl -X "POST" "$DATAPLATFORM_IP:8083/connectors" \
     -H "Content-Type: application/json" \
     -d '{
  "name": "customer.jdbcsrc.cdc",
  "config": {
    "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
    "tasks.max": "1",
    "connection.url": "jdbc:postgresql://postgresql/customer_db?user=customer&password=abc123!",
    "mode": "timestamp",
    "timestamp.column.name": "modified_at",
    "poll.interval.ms": "10000",
    "table.whitelist": "public.person, public.address, public.country, public.card",
    "validate.non.null": "false",
    "topic.prefix": "priv.customer.",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "key.converter.schemas.enable": "false",
    "value.converter": "io.confluent.connect.avro.AvroConverter",
    "value.converter.schema.registry.url": "http://schema-registry-1:8081",    
    "name": "customer.jdbcsrc.cdc",
    "transforms": "createKey,extractInt,addSuffix",
    "transforms.createKey.type": "org.apache.kafka.connect.transforms.ValueToKey",
    "transforms.createKey.fields": "id",
    "transforms.extractInt.type": "org.apache.kafka.connect.transforms.ExtractField$Key",
    "transforms.extractInt.field": "id",
    "transforms.addSuffix.type": "org.apache.kafka.connect.transforms.RegexRouter",
    "transforms.addSuffix.regex": ".*",
    "transforms.addSuffix.replacement": "$0.cdc.v1"
  }
}'
```

```bash
kcat -b localhost -t priv.customer.person.cdc.v1 -q -r http://localhost:8081 -s value=avro
```

### Log-based CDC using Debezium and Kafka Connect


```
curl -X PUT \
  "http://$DATAPLATFORM_IP:8083/connectors/customer.dbzsrc.cdc/config" \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
  "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
  "tasks.max": "1",
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
  "transforms":"dropPrefix",  
  "transforms.dropPrefix.type": "org.apache.kafka.connect.transforms.RegexRouter",  
  "transforms.dropPrefix.regex": "customer.public.(.*)",  
  "transforms.dropPrefix.replacement": "priv.$1.dbz.v2",
  "topic.creation.default.replication.factor": 3,
  "topic.creation.default.partitions": 2,
  "topic.creation.default.cleanup.policy": "compact"
}'
```

### Transactional Outbox Pattern using Debezium and Kafka Connect

```
curl -X PUT \
  "http://$DATAPLATFORM_IP:8083/connectors/cardHolder.dbzsrc.outbox/config" \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
  "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
  "tasks.max": "1",

  "database.server.name": "postgresql",
  "database.port": "5432",
  "database.user": "customer",
  "database.password": "abc123!",
  "database.dbname": "customer_db",
  "topic.prefix": "cardHolder",
  "schema.include.list": "public",
  "table.include.list": "public.outbox",
  "plugin.name": "pgoutput",
  "publication.name":"debezium",
  "slot.name":"debezium",
  "tombstones.on.delete": "false",
  "database.hostname": "postgresql",
  "transforms": "outbox",
  "transforms.outbox.type": "io.debezium.transforms.outbox.EventRouter",
  "transforms.outbox.table.field.event.id": "id",
  "transforms.outbox.table.field.event.key": "event_key",
  "transforms.outbox.table.field.event.payload": "payload_avro",
  "transforms.outbox.route.by.field": "event_type",
  "transforms.outbox.route.topic.replacement": "pub.customer.${routedByValue}.state.v1",
  "value.converter": "io.debezium.converters.BinaryDataConverter",
  "topic.creation.default.replication.factor": 3,
  "topic.creation.default.partitions": 8,
  "key.converter": "org.apache.kafka.connect.storage.StringConverter"
}'
```

## Merchant to Iceberg


```bash
docker exec -ti spark-master spark-sql
```

```sql
use hiverest;

CREATE DATABASE IF NOT EXISTS payment_db
LOCATION 's3a://lakehouse-bucket/payment_db';

CREATE TABLE payment_db.merchant_t (
    merchantId STRING,
    name STRING);
    
CREATE TABLE payment_db.merchant_category_t (
    categoryId STRING,
    name STRING);    
```

```bash
#!/bin/bash

curl -X PUT \
  http://$DATAPLATFORM_IP:8083/connectors/pay-merchant-kafka-to-s3/config \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
      "connector.class": "org.apache.iceberg.connect.IcebergSinkConnector",
      "tasks.max": "1",
      "topics": "pub.merchant.merchant.state.v1",
      "iceberg.tables": "payment_db.merchant_t",
      "iceberg.tables.dynamic-enabled": "false",
      "write.upsert.enabled": "false",
      "iceberg.control.commit.interval-ms": "60000",
      "consumer.max.poll.records": "5000",
      "iceberg.catalog.type": "rest",
      "iceberg.catalog.uri": "http://hive-metastore:9084/iceberg",
      "iceberg.catalog.warehouse": "s3a://lakehouse-bucket/payment_db",      
      "iceberg.catalog.client.region": "us-east-1",
      "iceberg.catalog.s3.endpoint": "http://minio-1:9000",
      "iceberg.catalog.s3.path-style-access": "true",
      "iceberg.catalog.s3.access-key-id": "admin",
      "iceberg.catalog.s3.secret-access-key": "abc123abc123!",
      "value.converter": "io.confluent.connect.avro.AvroConverter",
      "value.converter.schema.registry.url": "http://schema-registry-1:8081",
      "key.converter": "org.apache.kafka.connect.storage.StringConverter"
	}'

curl -X PUT \
  http://$DATAPLATFORM_IP:8083/connectors/pay-merchant-category-kafka-to-s3/config \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
      "connector.class": "org.apache.iceberg.connect.IcebergSinkConnector",
      "tasks.max": "1",
      "topics": "pub.merchant.category.state.v1",
      "iceberg.tables": "payment_db.merchant_category_t",
      "iceberg.tables.dynamic-enabled": "false",
      "write.upsert.enabled": "false",
      "iceberg.control.commit.interval-ms": "60000",
      "consumer.max.poll.records": "5000",
      "iceberg.catalog.type": "rest",
      "iceberg.catalog.uri": "http://hive-metastore:9084/iceberg",
      "iceberg.catalog.warehouse": "s3a://lakehouse-bucket/payment_db",      
      "iceberg.catalog.client.region": "us-east-1",
      "iceberg.catalog.s3.endpoint": "http://minio-1:9000",
      "iceberg.catalog.s3.path-style-access": "true",
      "iceberg.catalog.s3.access-key-id": "admin",
      "iceberg.catalog.s3.secret-access-key": "abc123abc123!",
      "value.converter": "io.confluent.connect.avro.AvroConverter",
      "value.converter.schema.registry.url": "http://schema-registry-1:8081",
      "key.converter": "org.apache.kafka.connect.storage.StringConverter"
	}'
```




## Customer to Iceberg

```bash
docker exec -ti spark-master spark-sql
```

```sql
use hiverest;

CREATE DATABASE customer_db
LOCATION 's3a://lakehouse-bucket/customer_db';

CREATE TABLE customer_db.customer_t (
  customer STRUCT<
    id: STRING,
    firstName: STRING,
    lastName: STRING,
    customerTier: STRING,
    avgTransactionAmount: INT,
    addresses: ARRAY<
      STRUCT<
        street: STRING,
        zipCode: STRING,
        city: STRING,
        state: STRING
      >
    >,
    usualCountries: ARRAY<STRING>,
    onboardedDate: TIMESTAMP
  >
);
```

```
#!/bin/bash

echo "removing S3 Iceberg Sink Connector"

curl -X "DELETE" "$DOCKER_HOST_IP:8083/connectors/crm-customer-kafka-to-s3"

echo "creating Confluent S3 Iceberg Sink Connector"

curl -X PUT \
  http://${DOCKER_HOST_IP}:8083/connectors/crm-customer-kafka-to-s3/config \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
      "connector.class": "org.apache.iceberg.connect.IcebergSinkConnector",
      "tasks.max": "1",
      "topics": "pub.crm.customer.state.v1",
      "iceberg.tables": "customer_db.customer_t",
      "iceberg.tables.dynamic-enabled": "false",
      "write.upsert.enabled": "false",
      "iceberg.control.commit.interval-ms": "60000",
      "consumer.max.poll.records": "5000",
      "iceberg.catalog.type": "rest",
      "iceberg.catalog.uri": "http://hive-metastore:9084/iceberg",
      "iceberg.catalog.warehouse": "s3a://lakehouse-bucket/customer_db",      
      "iceberg.catalog.client.region": "us-east-1",
      "iceberg.catalog.s3.endpoint": "http://minio-1:9000",
      "iceberg.catalog.s3.path-style-access": "true",
      "iceberg.catalog.s3.access-key-id": "admin",
      "iceberg.catalog.s3.secret-access-key": "abc123abc123!",
      "value.converter": "io.confluent.connect.avro.AvroConverter",
      "value.converter.schema.registry.url": "http://schema-registry-1:8081",
      "key.converter": "org.apache.kafka.connect.storage.StringConverter"
	}'
```



## Spark SQL


```python
import os
# get the accessKey and secretKey from Environment
accessKey = os.environ['AWS_ACCESS_KEY_ID']
secretKey = os.environ['AWS_SECRET_ACCESS_KEY']

from pyspark.sql import SparkSession
spark = (
    SparkSession.builder
        .appName("Jupyter")
        .master("local[*]")

        .config("spark.jars.packages",
                "org.apache.iceberg:iceberg-spark-runtime-4.0_2.13:1.10.1,"
                "org.apache.iceberg:iceberg-aws-bundle:1.10.1")

        # Iceberg catalog
        .config("spark.sql.catalog.hiverest", "org.apache.iceberg.spark.SparkCatalog")
        .config("spark.sql.catalog.hiverest.type", "rest")
        .config("spark.sql.catalog.hiverest.uri", "http://hive-metastore:9084/iceberg")
        .config("spark.sql.catalog.hiverest.io-impl", "org.apache.iceberg.aws.s3.S3FileIO")
        .config("spark.sql.catalog.hiverest.warehouse", "s3a://admin-bucket/iceberg/warehouse")

        # ⭐ REQUIRED FOR MINIO WITH ICEBERG AWS SDK
        .config("spark.sql.catalog.hiverest.s3.endpoint", "http://minio-1:9000")
        .config("spark.sql.catalog.hiverest.s3.path-style-access", "true")
        .config("spark.sql.catalog.hiverest.s3.access-key-id", accessKey)
        .config("spark.sql.catalog.hiverest.s3.secret-access-key", secretKey)

        .config("spark.sql.defaultCatalog", "hiverest")

        .config(
            "spark.sql.extensions",
            "org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions"
        )

        .getOrCreate()
)
```

```
%load_ext sql
%sql spark
```

```
%%sql
select 1
```



                "bookingText": {
                    "_gen": "string",
                    "expr": "#{merchantName} #{city} #{countryCode}"
                },


```python
from pyspark.sql import functions as F

# Load tables as DataFrames
tra = spark.table("payment_db.transaction_t")
mer = spark.table("payment_db.merchant_t")
cat = spark.table("payment_db.merchant_category_t")

df = (
    tra.alias("tra")
    .join(
        mer.alias("mer"),
        F.col("tra.merchantId") == F.col("mer.merchantId"),
        "left"
    )
    .join(
        cat.alias("cat"),
        F.col("tra.merchantCategoryId") == F.col("cat.categoryId"),
        "left"
    )
    .select(
        F.col("tra.transactionId").alias("transaction_id"),
        F.col("tra.card_number").alias("card_number"),
        F.col("tra.country").alias("country"),
        F.col("tra.city").alias("city"),
        F.col("tra.currency").alias("currency"),
        F.col("tra.amount").alias("amount"),
        F.col("tra.channel").alias("channel"),
        F.col("tra.timestamp").alias("timestamp"),
        F.col("mer.name").alias("merchant_name"),
        F.col("cat.name").alias("category_name"),
    )
)
```

### Cities data for geo-location enrichment

```
from pyspark.sql.types import *

schema = StructType([
    StructField("geonameid", LongType()),
    StructField("name", StringType()),
    StructField("asciiname", StringType()),
    StructField("alternatenames", StringType()),
    StructField("latitude", DoubleType()),
    StructField("longitude", DoubleType()),
    StructField("feature_class", StringType()),
    StructField("feature_code", StringType()),
    StructField("country_code", StringType()),
    StructField("cc2", StringType()),
    StructField("admin1_code", StringType()),
    StructField("admin2_code", StringType()),
    StructField("admin3_code", StringType()),
    StructField("admin4_code", StringType()),
    StructField("population", LongType()),
    StructField("elevation", IntegerType()),
    StructField("dem", IntegerType()),
    StructField("timezone", StringType()),
    StructField("modification_date", StringType())
])

df = (
    spark.read
        .option("sep", "\t")
        .schema(schema)
        .csv("s3a://admin-bucket/geonames/cities1000.txt")
)



```