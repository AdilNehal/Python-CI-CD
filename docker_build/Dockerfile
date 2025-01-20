FROM jenkins/inbound-agent:latest
USER root
RUN apt-get update && apt-get install curl && \
  apt-get install -y groovy && \
  apt-get install -y python3 && \
  curl https://get.docker.com/builds/Linux/x86_64/docker-latest.tgz | tar xvz -C /tmp/ && mv /tmp/docker/docker /usr/bin/docker && \
  apt-get clean
USER jenkins