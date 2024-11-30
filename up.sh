set -e

echo "Checking if PostgreSQL is up..."
TIMEOUT=10
COUNTER=0

until pg_isready -h localhost -p 5432; do
  COUNTER=$((COUNTER + 2))
  if [ $COUNTER -ge $TIMEOUT ]; then
    echo "PostgreSQL is not up after $TIMEOUT seconds. Exiting."
    exit 1
  fi
  echo "Waiting for PostgreSQL to be ready... ($COUNTER/$TIMEOUT seconds)"
  sleep 1
done

echo "PostgreSQL is up and running!"

echo "Running Maven clean install..."
mvn clean install

echo "Starting Docker services using docker-compose..."
docker-compose up --build -d

echo "All done!"