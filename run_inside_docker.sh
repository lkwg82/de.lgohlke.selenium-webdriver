#!/bin/bash

set -e

# just for resolving all dependencies
#mvn install -DskipTests

# actual running tests
mvn clean
#timeout --preserve-status --kill-after 5m 4m mvn verify -Dxvfb.display=20
mvn test
