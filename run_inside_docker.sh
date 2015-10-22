#!/bin/bash

set -e

# just for resolving all dependencies
#mvn install -DskipTests

# actual running tests
#mvn clean
mvn integration-test -Dit.test=ChromeDriverServiceFactoryIT -Dxvfb.display=20
