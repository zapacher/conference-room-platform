#!/bin/bash
set -e

MODE=$1

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

if [[ $MODE == "silent" ]]; then
    echo "Maven install in silent mode..."
    mvn clean install >  /dev/null 2>&1
else
    mvn clean install
fi

if [[ $MODE == "silent" ]]; then
    echo "Docker starting  in silent mode..."
    docker-compose up -d >  /dev/null 2>&1
else
    echo "Starting Docker services using docker-compose..."
    docker-compose up
fi

echo "All done!"