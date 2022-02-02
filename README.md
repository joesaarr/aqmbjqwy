# Bank
Ths is a small core banking solution to keep track of current accounts, balances, and transaction
history.

## Building and running
#### Prerequisites
* Docker
* Java

#### Building
Change directory:
```
cd bank-src
```
Build .jar:
```
./gradlew build
```
Build docker image:

```
docker build --build-arg JAR_FILE=app/build/libs/app.jar -t bank-app .
```

#### Running

run docker containers:
```
docker-compose -f ..\docker\docker-compose.yml up
```
The application should run on http://localhost:8081/
## API endpoints
* ### POST /account
**Creates a bank account for the customer and returns an account object
together with balance objects.**

Example request with curl:
```
curl --location --request POST 'localhost:8081/account' --header 'Content-Type: application/json' --data-raw '{"customerId": "CUSTOMERID1", "country": "EE", "currencies": ["EUR", "GBP"]}'
```
* ### GET /account/{accountId}
**Returns a bank account object of the customer together with balance objects.**

Example request: http://localhost:8081/account/1
* ### POST /transaction
**Creates a transaction on the account and returns the transaction object.**

Example request with curl:
```
curl --location --request POST 'localhost:8081/transaction' \
--header 'Content-Type: application/json' \
--data-raw '{
"accountId": 1,
"amount": 1000,
"currency": "USD",
"direction": "IN",
"description": "Transaction description"
}'
```
* ###GET /transaction/{accountId}
**Returns a list of transactions of the customer.**

Example request: http://localhost:8081/transaction/1

## Choices

Messages are published to an exchange named "bank" with routing key "create-account"
or "create-transaction" so that listeners can bind routing keys
of their choice to queues declared by them.

'Testcontainers' Java library is used in order to run tests with
a dockerized PostgreSQL database that is similar to a database in production environment.

Hibernate validator is used to conveniently validate most of the input of api requests.

Otherwise, the solution is rather standard given the provided technologies.

## Operating capacity

The application is able to handle approximately 2000
new transactions per second on my development machine.

## Horizontal scaling

It is fairly easy to set up multiple instances of the java application in
different servers. If a load-balancer is balancing request between different
servers it is important that the java application is stateless. This application
is stateless.

Keeping different application layers independent will allow to separately scale
each component of the app in order to eliminate possible bottlenecks. Each service
should be self-contained.

