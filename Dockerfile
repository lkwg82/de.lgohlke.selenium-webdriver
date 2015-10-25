FROM lkwg82/mitmproxy-0.11-maven3-jdk8

RUN apt-get update \
    && apt-get install -y xterm     `# for testing xvfb running` \
    ;

ADD . /home/build
WORKDIR /home/build
RUN dpkg --list | grep ^ii > installed_software.log

