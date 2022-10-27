# Prepare docker database
docker ps 2> $null
if ($LASTEXITCODE -ne 0) {
    echo "Docker daemon is not running, make sure it is running and then rerun this script"
    exit 1
}
docker stop to-db 2> $null
docker rm to-db 2> $null
docker run --name to-db -e POSTGRES_PASSWORD=admin --rm -p 5431:5432 -d postgres
docker cp src/test/resources/users_schema.sql to-db:/docker-entrypoint-initdb.d/dump.sql
Start-Sleep 2
docker exec --user postgres -it to-db psql postgres postgres -f /docker-entrypoint-initdb.d/dump.sql
