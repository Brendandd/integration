@echo off
REM Check if the shared network exists, and create it if it doesn't
docker network inspect integration-net >nul 2>&1 || (
    echo Network "integration-net" does not exist. Creating...
    docker network create integration-net
)

docker compose -f ../../../apache-activemq-artemis/docker-compose.yml --env-file ../.env.example up --build