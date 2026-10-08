# Jenkins controller used by the SafeStep CI/CD pipeline.
# Based on the course image (jenkins-maven-sonar-jdk): Jenkins LTS + Maven + Docker CLI + the JDK used to build
# the project. The pipeline selects JDK 26 through JAVA_HOME_26, so no global tool configuration is needed.
FROM jenkins/jenkins:lts-jdk25

USER root

RUN apt-get update && \
    apt-get install -y git curl wget tar apt-transport-https ca-certificates gnupg lsb-release && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Docker CLI only: the pipeline talks to the host daemon through the mounted /var/run/docker.sock
RUN mkdir -p /etc/apt/keyrings && \
    curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg && \
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/debian $(lsb_release -cs) stable" > /etc/apt/sources.list.d/docker.list && \
    apt-get update && apt-get install -y docker-ce-cli && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Maven 3.9 (same version as the application Dockerfile)
ENV MAVEN_VERSION=3.9.11
RUN curl -fsSL https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    | tar xzf - -C /opt && ln -s /opt/apache-maven-${MAVEN_VERSION}/bin/mvn /usr/local/bin/mvn

# JDK 26 (Eclipse Temurin) to compile and test the project
RUN mkdir -p /opt/java && \
    wget -qO- https://api.adoptium.net/v3/binary/latest/26/ga/linux/x64/jdk/hotspot/normal/eclipse | tar -xz -C /opt/java && \
    mv /opt/java/jdk-26* /opt/java/jdk-26
ENV JAVA_HOME_26=/opt/java/jdk-26

# The repository is mounted from the host, owned by another user
RUN git config --system --add safe.directory '*'

USER jenkins

COPY --chown=jenkins:jenkins plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# Skip the setup wizard (the controller is configured as code in casc.yaml) and allow the job to check out the
# repository mounted from the host (file:// remotes are blocked by default)
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false -Dhudson.plugins.git.GitSCM.ALLOW_LOCAL_CHECKOUT=true"
ENV CASC_JENKINS_CONFIG=/usr/share/jenkins/ref/casc.yaml
COPY --chown=jenkins:jenkins casc.yaml /usr/share/jenkins/ref/casc.yaml
