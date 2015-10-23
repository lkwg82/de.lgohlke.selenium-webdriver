#!/bin/bash

set -e
set -x
# $ export MAVEN_REPODIR="-v /tmp/m2_docker:/root/.m2/repository"; ./run_tests.sh

if [ -z "$MAVEN_REPODIR" ]; then
    export MAVEN_REPODIR="-v /tmp/m2_docker:/root/.m2/repository";
fi

args="--name=webdriver-test-$$ -m 1G --memory-swap=-1 -v /tmp/webdriver-tests-$USER:/tmp/webdrivers-root -v /dev/shm/:/dev/shm -v /tmp/webdrivers_docker:/root/tmp_webdrivers $MAVEN_REPODIR "

rm -rf docker_target
docker build -t test-$$ . | tee docker_build.log
export IMAGE_ID=$(tail -n1 docker_build.log| cut -d\  -f3)
export CID=$(docker run -d $args $IMAGE_ID bash -c 'while(true);do sleep 1;done')

echo -n $IMAGE_ID > docker_IMAGE_ID
echo -n $CID > docker_CID

function cleanup {
  docker cp $CID:/home/build/target docker_target

  #docker kill $CID
  docker rm  -f $CID
  docker rmi  $IMAGE_ID
}
trap cleanup EXIT INT

docker exec $CID ./run_inside_docker.sh


