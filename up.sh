#!/bin/bash
set -e


SKIP_WAIT=false
SILENT_MODE=false
TESTS_MODE=true

while getopts "fst" opt; do
  case ${opt} in
    t)
      TESTS_MODE=false
      ;;
    f)
      SKIP_WAIT=true
      ;;
    s)
      SILENT_MODE=true
      ;;
    \?)
      echo "Invalid option: -$OPTARG" 1>&2
      ;;
  esac
done

echo "Checking if PostgreSQL is up..."
if [ "$SKIP_WAIT" = false ]; then
  TIMEOUT=10
  COUNTER=0
  while ! pg_isready -h localhost -p 5432; do
    COUNTER=$((COUNTER + 2))
    if [ $COUNTER -ge $TIMEOUT ]; then
      echo "PostgreSQL is not up after $TIMEOUT seconds. Exiting."
      exit 1
    fi
    echo "Waiting for PostgreSQL to be ready... ($COUNTER/$TIMEOUT seconds)"
    sleep 1
  done
  echo "PostgreSQL is up and running!"
else
  echo "PostgreSQL check is ignored!"
fi

if [ "$SILENT_MODE" = true ]; then
    echo "Maven install in silent mode..."
    if [ "$TESTS_MODE" = true ]; then
      mvn clean install >  /dev/null 2>&1
    else
      echo "Tests are ignored -DskipTests"
      mvn clean install -DskipTests >  /dev/null 2>&1
    fi
else
     if [ "$TESTS_MODE" = true ]; then
          mvn clean install
        else
          echo "Tests are ignored -DskipTests"
          mvn clean install -DskipTests
        fi
fi

if [ "$SILENT_MODE" = true ]; then
    echo "Docker starting  in silent mode..."
    docker-compose up -d >  /dev/null 2>&1
else
    echo "Starting Docker services using docker-compose..."
    docker-compose up
fi

echo "All done!"