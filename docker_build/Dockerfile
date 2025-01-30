# Base image for Jenkins agent
FROM jenkins/inbound-agent:latest AS base
USER root

# Install necessary tools
RUN apt-get update && \
    apt-get install -y curl groovy python3 git && \
    curl -fsSL https://get.docker.com/builds/Linux/x86_64/docker-latest.tgz | tar xvz -C /tmp/ && \
    mv /tmp/docker/docker /usr/bin/docker && \
    git config --global user.email "adilnehal2000@gmail.com" && \
    git config --global user.name "AdilNehal" && \
    apt-get clean

# Final image with Trivy
FROM base
USER root

# Install Trivy
RUN curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- -b /usr/local/bin

USER jenkins
