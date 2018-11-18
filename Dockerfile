FROM ubuntu:16.04

RUN apt-get update
RUN apt-get install --no-install-recommends -y chromium-browser chromium-chromedriver libgconf-2-4
RUN apt-get install --no-install-recommends -y openjdk-8-jdk

ADD . /home/build
WORKDIR /home/build
RUN dpkg --list | grep ^ii > installed_software.log

