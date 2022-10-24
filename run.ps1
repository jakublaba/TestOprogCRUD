# Prepare docker database
docker ps 2> $null
if ($LASTEXITCODE -ne 0) {
    echo "Docker daemon is not running, make sure it is running and then rerun this script"
    exit
}
docker stop to-db 2> $null
docker rm to-db 2> $null
docker run --name to-db -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin --rm -p 5431:5432 -d postgres

# Run tests
./gradlew clean test