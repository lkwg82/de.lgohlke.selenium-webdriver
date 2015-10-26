#!/bin/bash

# just for resolving all dependencies
mvn install -DskipTests

# actual running tests
timeout --preserve-status --kill-after 7m 6m \
    mvn clean verify -P sonar-coverage