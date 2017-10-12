#!/bin/bash

# just for resolving all dependencies
./mvnw install -DskipTests

# actual running tests
timeout --preserve-status --kill-after 7m 6m \
    ./mvnw clean verify -P sonar-coverage