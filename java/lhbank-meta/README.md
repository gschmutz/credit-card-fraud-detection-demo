# card-meta

Generate customers according to avro schema

```
{
            "name": "customers",
            "connection": "kafka",
            "vars": {
                "customerId": {
                    "_gen": "uuid"
                }
            },
            "topic": "pub.crm.customer.state.v1",
            "key": {
                "_gen": "var", "var": "customerId"
            },
            "value": {
                "customer": {
                    "id": {
                        "_gen": "var", "var": "customerId"
                    },
                    "firstName": {
                        "_gen": "string",
                        "expr": "#{Name.firstName}"
                    },
                    "lastName": {
                        "_gen": "string",
                        "expr": "#{Name.lastName}"
                    },
                    "avgTransactionAmount": {
                        "_gen": "weightedOneOf",
                        "choices": [
                            {
                                "weight": 19,
                                "value": {
                                    "_gen": "uniformDistribution",
                                    "bounds": [1, 1000],
                                    "decimals": 0
                                }
                            },
                            {
                                "weight": 1,
                                "value": {
                                    "_gen": "normalDistribution",
                                    "mean": 3000,
                                    "sd": 500,
                                    "decimals": 0
                                }
                            }
                        ]
                    },
                    "customerTier": {
                        "_gen": "oneOf",
                        "choices": [
                            "bronze",
                            "silver",
                            "gold",
                            "platinum"
                        ]
                    },
                    "addresses" : {
                        "_gen" : "repeatedly",
                        "n" : {
                        "_gen" : "uniformDistribution",
                        "bounds" : [
                            1,
                            5
                        ],
                        "decimals" : 0
                        },
                        "target" : {
                        "street" : {
                            "_gen" : "string",
                            "expr" : "#{Address.streetName}"
                        },
                        "zipCode" : {
                            "_gen" : "string",
                            "expr" : "#{Address.zipCode}"
                        },
                        "city" : {
                            "_gen" : "string",
                            "expr" : "#{Address.city}"
                        },
                        "state" : {
                            "_gen" : "string",
                            "expr" : "#{Team.state}"
                        }
                        }
                    },                    
                    "usualCountries": {
                        "_gen": "repeatedly",
                        "n": 5, 
                        "target":{
                            "_gen": "oneOf",
                            "choices": [
                                "DE",
                                "CH",
                                "GB",
                                "IE",
                                "FR",
                                "BE",
                                "NL",
                                "IT",
                                "GI",
                                "MT",
                                "AT",
                                "ES",
                                "PT",
                                "LI",
                                "DK",
                                "SE",
                                "NO",
                                "FI"
                            ]
                        },
                        "sample": {
                            "rate": 0.5
                        }
                    },
                    "activationDate": {
                        "_gen": "now"
                    }
                }
            },
            "localConfigs": {
                "throttleMs": {
                    "_gen": "uniformDistribution",
                    "bounds": [
                        2000,
                        10000
                    ]
                },
                "avroSchemaHint" : {
                    "value" : {
                        "type" : "record",
                        "name" : "CustomerState",
                        "namespace" : "com.lhbank.card.customer.avro",
                        "fields" : [
                        {
                            "name" : "customer",
                            "type" : {
                            "type" : "record",
                            "name" : "Customer",
                            "fields" : [
                                {
                                "name" : "id",
                                "type" : "string"
                                },
                                {
                                "name" : "firstName",
                                "type" : "string"
                                },
                                {
                                "name" : "lastName",
                                "type" : "string"
                                },
                                {
                                "name" : "customerTier",
                                "type" : "string"
                                },
                                {
                                "name" : "avgTransactionAmount",
                                "type" : "long"
                                },
                                {
                                "name" : "addresses",
                                "type" : {
                                    "type" : "array",
                                    "items" : {
                                    "type" : "record",
                                    "name" : "Address",
                                    "fields" : [
                                        {
                                        "name" : "street",
                                        "type" : "string"
                                        },
                                        {
                                        "name" : "zipCode",
                                        "type" : "string"
                                        },
                                        {
                                        "name" : "city",
                                        "type" : "string"
                                        },
                                        {
                                        "name" : "state",
                                        "type" : "string"
                                        }
                                    ]
                                    }
                                }
                                },
                                {
                                "name" : "usualCountries",
                                "type" : {
                                    "type" : "array",
                                    "items" : "string"
                                }
                                },
                                {
                                "name" : "activationDate",
                                "type" : {
                                    "type" : "long",
                                    "logicalType" : "timestamp-millis"
                                }
                                }
                            ]
                            }
                        }
                        ]
                    }
                }
            }
        },
```