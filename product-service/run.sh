#!/bin/bash

# Load .env file
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
    echo ".env loaded successfully"
else
    echo "No .env file found!"
    exit 1
fi

# Run the service
mvn spring-boot:run