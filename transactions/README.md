# Transactions with Kafka and Spring Boot

This module shows how to work with Kafka and Database transactions in Kafka and Spring Boot.

## Transfer Microservice
It is a REST API that acts as Kafka Producer which create a transfer. Once the transfer is processing are executed next steps:
- The transfer details are saved into database.
- It is created a deposit event and published into Kafka topic (deposit-money-topic).
- It is called the mock service.
- It is created a withdrawal event and published into Kafka topic (withdraw-money-topic).
- If all before steps are executed successfully the transaction is marked as completed 
  into Kafka topic and DataBase.
- If any of the before steps fails the transaction is marked as failed 
  into Kafka topic and DataBase.

## Deposit Microservice
It is a Kafka consumer which consumes the deposit event created by Transfer Microservice.
If the deposit event cannot be deserialized so the consumer sends the event to Dead Letter Topic (DLT).

## Withdraw Microservice
It is a Kafka consumer which consumes the withdrawal event created by Transfer Microservice.
Note: If the withdrawal event cannot be deserialized so the consumer sends the event to Dead Letter Topic (DLT).

## Dead Letter Topic (DLT)
It is a Kafka topic which stores the events that cannot be deserialized. 
By default, the topic name it will be the topic name + . + DLT. For example:
- deposit-money-topic.DLT
- withdraw-money-topic.DLT

## Mock Service
It is a REST API to simulate other call made by Transfer Microservice.
This is only for testing purposes to show how handle exceptions to retry events with Kafka. 

## Core
It is a library which contains the domain model events used by Transfer Microservice, Deposit and Withdraw consumers.

All of these examples are based on the course "Apache Kafka for Event-Driven Microservices" by [Udemy](https://www.udemy.com/).
