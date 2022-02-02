# aqmbjqwy

cd bank-src
# build .jar with gradle
gradle build

# build docker image
docker build --build-arg JAR_FILE=app/build/libs/app.jar -t bank-app .

# run docker containers
docker-compose -f ../docker/docker-compose.yml up

# API endpoints
POST /account

curl --location --request POST 'localhost:8081/account' \
--header 'Content-Type: application/json' \
--data-raw '{
	"customerId": "CUSTOMERID1",
	"country": "EE",
	"currencies": ["EUR", "GBP"]
}'

GET /account/{accountId}


POST /transaction


GET /transaction{accountId}

# choices

# load

# horizontal scaling


