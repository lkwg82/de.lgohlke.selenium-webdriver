FROM lkwg82/mitmproxy-0.11-maven3-jdk8

#RUN useradd -m build
#USER build
ADD . /home/build
WORKDIR /home/build
