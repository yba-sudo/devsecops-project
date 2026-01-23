#!/bin/bash
echo "Testing Docker configuration..."
docker --version
docker-compose --version
echo "Building image..."
docker build -t devsecops-backend:test .
echo "Running containers..."
docker-compose up -d
sleep 10
echo "Testing API..."
curl -f http://localhost:8080/api/health
curl -f http://localhost:8080/api/hello
echo "Cleaning up..."
docker-compose down
