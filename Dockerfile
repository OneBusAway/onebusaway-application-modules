#
# Copyright (C) 2024 Open Transit Software Foundation <info@onebusaway.org>
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

FROM golang:1.24.1-bookworm AS gobuilder

# Install gtfstidy - https://github.com/patrickbr/gtfstidy
WORKDIR /src
COPY ./docker_app_server/set_goarch.sh .
RUN ./set_goarch.sh
RUN CGO_ENABLED=0 go install github.com/patrickbr/gtfstidy@latest

FROM tomcat:8.5.100-jdk11-temurin-focal AS server

ARG MAVEN_VERSION=3.9.9

ARG OBA_VERSION=2.6.0
ENV OBA_VERSION=$OBA_VERSION

ENV CATALINA_HOME="/usr/local/tomcat"
ENV CATALINA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
ENV TZ="America/Los_Angeles"
ENV MAVEN_HOME="/usr/share/maven"
ENV MAVEN_CONFIG="/root/.m2"
ENV GTFS_URL="https://www.soundtransit.org/GTFS-KCM/google_transit.zip"
ENV PATH="/oba:${PATH}"

# Install additional necessary tools
RUN apt-get update && apt-get install -y \
    curl \
    git \
    jq \
    python3-pip \
    supervisor \
    tzdata \
    unzip \
    vim \
    zip \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Install Maven
RUN mkdir -p /usr/share/maven /usr/share/maven/ref && \
    curl -fsSL -o /tmp/apache-maven.tar.gz https://downloads.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
    tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 && \
    rm -f /tmp/apache-maven.tar.gz && \
    ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

RUN mkdir -p $MAVEN_CONFIG/repository

# Set up bundle builder
WORKDIR /oba
COPY ./docker_app_server/bundle_builder/build_bundle.sh .
COPY ./docker_app_server/copy_resources.sh .

# Copy the gtfstidy binary over from gobuilder
COPY --from=gobuilder /go/bin/gtfstidy .

# Set the configured time zone
RUN ln -fs /usr/share/zoneinfo/$TZ /etc/localtime && dpkg-reconfigure -f noninteractive tzdata

# Set up the host-manager and manager webapps
RUN rm -rf $CATALINA_HOME/webapps
RUN mv $CATALINA_HOME/webapps.dist $CATALINA_HOME/webapps

COPY ./docker_app_server/config/tomcat-users.xml $CATALINA_HOME/conf/

COPY ./docker_app_server/config/host-manager_context.xml $CATALINA_HOME/webapps/docs/META-INF/context.xml
COPY ./docker_app_server/config/host-manager_context.xml $CATALINA_HOME/webapps/examples/META-INF/context.xml
COPY ./docker_app_server/config/host-manager_context.xml $CATALINA_HOME/webapps/host-manager/META-INF/context.xml
COPY ./docker_app_server/config/host-manager_context.xml $CATALINA_HOME/webapps/manager/META-INF/context.xml
COPY ./docker_app_server/config/host-manager_context.xml $CATALINA_HOME/webapps/ROOT/META-INF/context.xml

# MySQL Connector
WORKDIR $CATALINA_HOME/lib
RUN wget "https://cdn.mysql.com/Downloads/Connector-J/mysql-connector-j-8.4.0.tar.gz" \
    && tar -zxvf mysql-connector-j-8.4.0.tar.gz \
    && mv mysql-connector-j-8.4.0/mysql-connector-j-8.4.0.jar . \
    && rm mysql-connector-j-8.4.0.tar.gz \
    && rm -rf mysql-connector-j-8.4.0

# Put the user in the source code directory when they shell in
WORKDIR /src

# Expose an additional port for debugging
EXPOSE 5005
