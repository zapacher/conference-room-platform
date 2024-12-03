

SILENT_MODE=false
PRUNE=false

while getopts "fs" opt; do
  case ${opt} in
    s)
      SILENT_MODE=true
      ;;
    f)
      PRUNE=true
      ;;
    \?)
      echo "Invalid option: -$OPTARG" 1>&2
      ;;
  esac
done

echo "Stopping docker containers"
docker-compose down --volumes
echo "Containers stopped"

if [ "$SILENT_MODE" = true ]; then
  echo "Maven cleaning silent"
  mvn clean > /dev/null 2>&1
else
  mvn clean
fi

if [ "$PRUNE" = true ]; then
  echo "Docker system prune"
  docker system prune -af
fi

echo "All done!"