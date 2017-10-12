FROM openjdk:8-jdk

RUN apt-get update \
    && apt-get install -y chromium libgconf-2-4;

ADD . /home/build
WORKDIR /home/build
RUN dpkg --list | grep ^ii > installed_software.log

