# platys-platform - List of Services

| Service | Links | External<br>Port | Internal<br>Port | Description
|--------------|------|------|------|------------
|[akhq](./documentation/services/akhq )|[Web UI](http://192.168.1.112:28107) - [Rest API](http://192.168.1.112:28107/api)|28107<br>28320<br>|8080<br>28081<br>|Kafka GUI
|[grafana](./documentation/services/grafana )|[Web UI](http://192.168.1.112:3000) - [Rest API](http://192.168.1.112:3000/api/org)|3000<br>|3000<br>|Data visualization and dashboarding
|[hive-metastore](./documentation/services/hive-metastore )||9083<br>9084<br>|9083<br>9084<br>|Hive Metastore
|[hive-metastore-db](./documentation/services/hive-metastore )||5442<br>|5432<br>|Hive Metastore DB
|[jikkou](./documentation/services/jikkou )||||Resource as Code framework for Apache Kafka
|[jupyter](./documentation/services/jupyter )|[Web UI](http://192.168.1.112:28888)|28888<br>28376-28380<br>|8888<br>4040-4044<br>|Web-based interactive development environment for notebooks, code, and data
|[kafka-1](./documentation/services/kafka )||9092<br>19092<br>29092<br>39092<br>9992<br>1234<br>|9092<br>19092<br>29092<br>39092<br>9992<br>1234<br>|Kafka Broker 1
|[kafka-2](./documentation/services/kafka )||9093<br>19093<br>29093<br>39093<br>9993<br>1235<br>|9093<br>19093<br>29093<br>39093<br>9993<br>1234<br>|Kafka Broker 2
|[kafka-3](./documentation/services/kafka )||9094<br>19094<br>29094<br>39094<br>9994<br>1236<br>|9094<br>19094<br>29094<br>39094<br>9994<br>1234<br>|Kafka Broker 3
|[kafka-connect-1](./documentation/services/kafka-connect )|[Rest API](http://192.168.1.112:8083)|8083<br>|8083<br>|Kafka Connect Node 1
|[kafka-connect-ui](./documentation/services/kafka-connect-ui )|[Web UI](http://192.168.1.112:28103)|28103<br>|8000<br>|Kafka Connect GUI
|[ksqldb-cli](./documentation/services/ksqldb-cli )||||ksqlDB Command Line Utility
|[ksqldb-server-1](./documentation/services/ksqldb )|[Rest API](http://192.168.1.112:8088)|8088<br>1095<br>|8088<br>1095<br>|ksqlDB Streaming Database - Node 1
|lhbank-cardholder-app||29000<br>|8082<br>|
|[markdown-viewer](./documentation/services/markdown-viewer )|[Web UI](http://192.168.1.112:80)|80<br>|3000<br>|Platys Platform homepage viewer
|[minio-1](./documentation/services/minio )|[Web UI](http://192.168.1.112:9010)|9000<br>9010<br>|9000<br>9010<br>|Software-defined Object Storage
|[minio-mc](./documentation/services/minio )||||MinIO Console
|[postgresql](./documentation/services/postgresql )||5432<br>|5432<br>|Open-Source object-relational database system
|[presto-1](./documentation/services/presto )|[Web UI](http://192.168.1.112:28081)|28081<br>|8080<br>|SQL Virtualization Engine
|[prometheus-1](./documentation/services/prometheus )|[Web UI](http://192.168.1.112:9090/graph) - [Rest API](http://192.168.1.112:9090/api/v1)|9090<br>|9090<br>|Monitoring system and time series database
|[schema-registry-1](./documentation/services/schema-registry )|[Rest API](http://192.168.1.112:8081)|8081<br>|8081<br>|Confluent Schema Registry
|[shadowtraffic](./documentation/services/shadowtraffic )||||Simulate production traffic to your backend.
|[spark-master](./documentation/services/spark )|[Web UI](http://192.168.1.112:28304)|28304<br>6066<br>7077<br>4040-4044<br>|28304<br>6066<br>7077<br>4040-4044<br>|Spark Master Node
|[spark-thriftserver](./documentation/services/spark-thriftserver )|[Web UI](http://192.168.1.112:28298)|28118<br>28298<br>|10000<br>4040<br>|Spark Thriftserver|

**Note:** init container ("init: true") are not shown