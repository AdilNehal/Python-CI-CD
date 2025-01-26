FROM jenkins/inbound-agent:latest
USER root

RUN apt-get update && \
    apt-get install -y curl groovy python3 && \
    curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh -o /tmp/install.sh && \
    chmod +x /tmp/install.sh && \
    /tmp/install.sh && \
    curl https://get.docker.com/builds/Linux/x86_64/docker-latest.tgz | tar xvz -C /tmp/ && \
    mv /tmp/docker/docker /usr/bin/docker && \
    apt-get clean

USER jenkins
