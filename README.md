# ez bank

## prerequisites
* docker

## building and running
Change directory:
```
cd bank-src
```
Build .jar:
```
gradle build
```
Build docker image:

```
docker build --build-arg JAR_FILE=app/build/libs/app.jar -t bank-app .
```
run docker containers:
```
docker-compose -f ..\docker\docker-compose.yml up
```
## API endpoints
### POST /account
Creates a bank account for the customer and returns an account object
together with balance objects.
```
curl --location --request POST 'localhost:8081/account' --header 'Content-Type: application/json' --data-raw '{"customerId": "CUSTOMERID1", "country": "EE", "currencies": ["EUR", "GBP"]}'
```
### GET /account/{accountId}
Returns a bank account object of the customer together with balance objects.
```
curl --location --request GET 'localhost:8081/account/1'
```
### POST /transaction
Creates a transaction on the account and returns the transaction object.
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
###GET /transaction/{accountId}
Returns a list of transactions of the customer.
```
curl --location --request GET 'localhost:8081/transaction/1'
```

# choices

# load

# horizontal scaling


