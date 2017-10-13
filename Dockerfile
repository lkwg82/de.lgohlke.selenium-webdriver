FROM ubuntu:16.04

RUN apt-get update && apt-get install -y chromium-browser chromium-chromedriver libgconf-2-4 openjdk-8-jdk;

ADD . /home/build
WORKDIR /home/build
RUN dpkg --list | grep ^ii > installed_software.log

