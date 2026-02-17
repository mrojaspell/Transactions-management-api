# Transaction Management
---
This project shows a Spring-Boot implementation of a simple REST API to manage transactions with hierarchical relationships and compute aggregated sums.


## Technologies Used
---
The following technology was used to implement the service:
* Java: A general-purpose programming language
* Spring Boot: a Java-based framework built on top of the Spring ecosystem that simplifies the process of creating stand-alone, production-ready applications with minimal configuration.

## Features:
---
* Creating transactions
* Linking transactions through parentId
* Retrieving transaction IDs by type
* Calculating the sum of a transaction and all its descendants

## Requirements
---
* Java 21+
* Maven

## Build
To build the appalication run:
`mvn clean install`
To run the application run:
`mvn spring-boot:run`
Server runs at:
`http://localhost:8080`

## Endpoints
* `PUT /transaction/{id}`
`Content-Type: application/json`
`{"amount": 100.0, "type": "cars", "parentId": null }`

* `GET /transactions/types/{type}`
* `GET /transactions/sum/{id}`