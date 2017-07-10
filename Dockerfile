FROM ubuntu:latest

MAINTAINER "Daniel Vargas" <dlvargas@stanford.edu>

# Install build tools
RUN apt-get update
RUN apt-get -y install git subversion ant gettext openjdk-8-jdk-headless maven locales

# Set LANG (needed for msginit -- called by lockss-daemon build.xml)
ENV LANG en_US.UTF-8
RUN locale-gen ${LANG}

# Get laaws-metadataservice source 
#RUN git clone https://gitlab.lockss.org/laaws/laaws-metadataservice.git --recursive
ADD . /laaws-metadataservice

# Build LOCKSS daemon JARs
WORKDIR /laaws-metadataservice
RUN ./initBuild

# XXX Isolate only what's needed to run
#RUN mkdir /laaws-metadataservice
#RUN mv target /laaws-metadataservice
#RUN mv runLaawsmetadataservice /laaws-metadataservice
#RUN mv src /laaws-metadataservice

# XXX Clean up 
RUN apt-get clean
RUN rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*
#RUN rm -rf ~/.m2 ~/.subversion

# XXX Ask fergaloy to fix
RUN mkdir logs
RUN touch logs/laawsmdq.log

# XXX Overlay needed for demo
ADD lockss.opt /laaws-metadataservice

CMD ["/bin/sh", "/laaws-metadataservice/buildAndRunLaawsMdq", "-Dswarm.http.port=8889", "-Djava.net.preferIPv4Stack=true"]
