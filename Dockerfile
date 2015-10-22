FROM lkwg82/mitmproxy-0.11-maven3-jdk8

RUN apt-get update && apt-get install -y libexif12

ADD . /home/build
WORKDIR /home/build
