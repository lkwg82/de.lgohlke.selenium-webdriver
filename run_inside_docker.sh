#!/bin/bash

set -e

# just for resolving all dependencies
#mvn install -DskipTests

# actual running tests
#mvn clean
timeout --preserve-status --kill-after 5m 4m mvn integration-test -Dit.test=DEBUGChromeDriverServiceFactoryIT -Dxvfb.display=20
