#!/bin/bash

set -e

# just for resolving all dependencies
mvn install -DskipTests

# actual running tests

mvn clean
timeout --preserve-status --kill-after 7m 6m mvn verify -P sonar-coverage