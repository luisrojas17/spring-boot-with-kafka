# saga-pattern-spring-boot-demo

This repository demonstrates how to implements SAGA Orchestration Design Pattern using Spring Boot and Kafka.

## Definition
The Saga Pattern is a failure management pattern used in Microservices Architecture to maintain data consistency across distributed systems without deal with global database locks (like Two-Phase Commit). It breaks a large transaction into a sequence of smaller local transactions, where each service updates its own database and triggers the next step via an event or message. If a local transaction fails (for any reason) then the saga executes a series of compensating transactions that undo the changes that were made by previous transactions.

There are two ways to implement SAGA:
1. Choreography - each local transaction publishes domain events that trigger local transactions in other services.
2. Orchestration - an orchestrator (object) tells the participants what local transactions to execute.

See: [Microservice Architecture - Saga Pattern by Chris Richardson](https://microservices.io/patterns/data/saga.html)


## Example Description

### Sequence of Steps 

1. Create a product - products service
2. Create an order - orders service
3. Reserve a product - orders service
4. Create a order history - orders service
5. Validate if the reserve product exist. - products service
6. If the reserve product exist it will created a product reserved - products service
7. If it occurs any exception it will be created a product reservation failed event - products service
8. If it was created a product reserved it will be created a payment process - orders service

## To run the docker-compose
```bash
docker compose -f docker-compose.yml up
```

kafka-console-consumer.sh --topic orders-events --bootstrap-server localhost:9092