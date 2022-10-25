#!/bin/bash

# Prepare docker database
docker ps 2> /dev/null
if [ $? -ne 0 ]; then
    echo "Docker daemon is not running, make sure it is running and then rerun this script"
    exit
fi
docker stop to-db 2> /dev/null
docker rm to-db 2> /dev/null
docker run --name to-db -e POSTGRES_PASSWORD=admin --rm -p 5431:5432 -d postgres &&
docker cp src/test/resources/users_schema.sql to-db:/docker-entrypoint-initdb.d/dump.sql && sleep 2 &&
docker exec --user postgres -it to-db psql postgres postgres -f /docker-entrypoint-initdb.d/dump.sql
