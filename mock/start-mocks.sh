#!/bin/bash
# Script to start the mock environment
echo "Starting mock services (ActiveMQ and PostgreSQL)..."
docker-compose up -d --build
echo "Mock services started."
