
echo "Stopping docker containers"
docker-compose down --volumes

echo "Maven clean"
mvn clean

echo "All done!"