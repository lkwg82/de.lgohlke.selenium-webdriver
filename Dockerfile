FROM lkwg82/mitmproxy-0.11-maven3-jdk8

ADD . /home/build
WORKDIR /home/build

ENTRYPOINT /bin/bash
