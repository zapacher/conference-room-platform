set -e

echo "Running Maven clean install..."
mvn clean install

echo "Starting Docker services using docker-compose..."
docker-compose up --build -d

echo "All done!"