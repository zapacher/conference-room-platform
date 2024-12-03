

SILENT_MODE=false

while getopts "fs" opt; do
  case ${opt} in
    s)
      SILENT_MODE=true
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

echo "All done!"