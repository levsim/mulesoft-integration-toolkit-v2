#!/bin/bash
# Script to stop the mock environment
echo "Stopping mock services..."
docker-compose down
echo "Mock services stopped."
