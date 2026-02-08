#!/bin/bash

export DATAPLATFORM_HOME=${PWD}/infra/platys
export SCRIPTS_DIR=${DATAPLATFORM_HOME}/../../scripts

wget https://hub-downloads.confluent.io/api/plugins/iceberg/iceberg-kafka-connect/versions/1.9.2/iceberg-iceberg-kafka-connect-1.9.2.zip -O ${DATAPLATFORM_HOME}/plugins/kafka-connect/connectors/iceberg-kafka-connect-1.9.2.zip
unzip -o -q ${DATAPLATFORM_HOME}/plugins/kafka-connect/connectors/iceberg-kafka-connect-1.9.2.zip -d ${DATAPLATFORM_HOME}/plugins/kafka-connect/connectors/
rm ${DATAPLATFORM_HOME}/plugins/kafka-connect/connectors/iceberg-kafka-connect-1.9.2.zip

cp ${SCRIPTS_DIR}/docker/*.yml ${DATAPLATFORM_HOME}/
cp ${SCRIPTS_DIR}/jikkou/*.* ${DATAPLATFORM_HOME}/scripts/jikkou/
cp ${SCRIPTS_DIR}/shadowtraffic/*.* ${DATAPLATFORM_HOME}/scripts/shadowtraffic/

# copy truststore to data-transfer folder
