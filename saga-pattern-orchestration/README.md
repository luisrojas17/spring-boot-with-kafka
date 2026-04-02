# saga-pattern-spring-boot-demo

Demonstration of SAGA Orchestration Design Pattern using Spring Boot and Kafka

1.- Create a product - products service
2.- Create an order - order service
3.- Reserve a product - order service
4.- Create a order history - order service

### To run the docker-compose
```bash
docker compose -f docker-compose.yml up
```

kafka-console-consumer.sh --topic orders-events --bootstrap-server localhost:9092