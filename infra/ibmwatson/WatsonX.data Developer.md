# WatsonX.data Developer

## Links

 * <https://github.com/IBM/watsonx-data> - Samples, tutorials and other information about watsonx.data
 * <https://github.com/IBM/watsonx-data-lab> - Documentation for watsonx.data Development Lab 
 * <https://github.com/IBM/dbt-watsonx-presto> - A DBT adapter for integrating DBT with watsonx.data Presto

## Setup



```
cd workspace/ibmwatson
```

Switch to bash

```
bash
```


```bash
export LH_ROOT_DIR=${PWD}
export LH_RELEASE_TAG=latest
export IBM_LH_TOOLBOX=cp.icr.io/cpopen/watsonx-data/ibm-lakehouse-toolbox:$LH_RELEASE_TAG
export LH_REGISTRY=cp.icr.io/cp/watsonx-data
export PROD_USER=cp
export IBM_ENTITLEMENT_KEY=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJJQk0gTWFya2V0cGxhY2UiLCJpYXQiOjE3MzgxNDIzOTgsImp0aSI6ImM0YTNlZTcwMWE3MjQxODJhYzA2Zjg5ZTBkYTJmZWRiIn0.E9oe6DiwyrBN9eFLXMEej4WlnlGskWyg6ux9c_Xatdo
export IBM_ICR_IO=cp.icr.io
```

```bash
export LH_REGISTRY=cp.icr.io/cp/watsonx-data
export DOCKER_EXE=docker
```

```bash
wget https://github.com/IBM/watsonx-data/releases/download/v2.2.1/ibm-lh-dev-2.2.1-432-20250814-111327-onprem-v2.2.1-amd64.tgz
``

```bash
tar -xvf ibm-lh-dev-2.2.1-432-20250814-111327-onprem-v2.2.1-amd64.tgz
```

```bash
docker login $LH_REGISTRY -u cp -p <entitlement key>
```

```bash
$LH_ROOT_DIR/ibm-lh-dev/bin/setup --license_acceptance=y --runtime=$DOCKER_EXE
``


```bash
$LH_ROOT_DIR/ibm-lh-dev/bin/start
$LH_ROOT_DIR/ibm-lh-dev/bin/start-milvus
$LH_ROOT_DIR/ibm-lh-dev/bin/expose-minio
$LH_ROOT_DIR/ibm-lh-dev/bin/expose-mds
```

Navigate to <https://localhost:9443/> and login as user `ibmlhadmin` with password `password`.

## WatsonX.data client package

```bash
wget https://github.com/IBM/watsonx-data/releases/download/v2.2.1/ibm-lh-client-2.2.1-432-20250814-111327-onprem-v2.2.1-amd64.tgz
```

```bash
tar -xvf ibm-lh-client-2.2.1-432-20250814-111327-onprem-v2.2.1-amd64.tgz
```

```bash
export LH_REGISTRY=cp.icr.io/cp/watsonx-data
```

```bash
docker login $LH_REGISTRY -u cp -p <entitilement>
```


```bash
./ibm-lh-client/bin/setup --license_acceptance=y --runtime=docker
```

## DBeaver

We will use the PrestoDB JDBC connector (NOT PrestoSQL). This is the other name for Trino, a variant of PrestoDB which might work. Select SQL (see Left side) and scroll down until you see PrestoDB.

Enter the following values into the dialog. Note: These settings are case-sensitive.

 * **Host** = `localhost`
 * **Port** = `8443`
 * **Username** = `ibmlhadmin`
 * **Password** = `password`
 * **Database** = `tpch`

Then select the Driver Properties tab. You might be asked to download the database driver. You need to enter three properties:

 * `SSL` = `true`
 * `SSLTrustStorePath` = `/Users/guido.schmutz/workspace/ibmwatson/ibm-lh-dev/localstorage/volumes/infra/tls/cert.crt`
 * `SSLTrustStorePassword` = `watsonx.data`


