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

FROM tomcat:8.5.100-jdk11-temurin

ENV CATALINA_HOME /usr/local/tomcat
ENV TZ=America/Los_Angeles
ARG OBA_VERSION=2.5.13-otsf
ENV OBA_VERSION=$OBA_VERSION
ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

RUN apt-get update && apt-get install -y \
    jq \
    supervisor \
    tzdata \
    unzip \
    xmlstarlet \
    python3-pip \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Set the configured time zone
RUN ln -fs /usr/share/zoneinfo/$TZ /etc/localtime && dpkg-reconfigure -f noninteractive tzdata

# Set up the host-manager and manager webapps
RUN rm -rf $CATALINA_HOME/webapps
RUN mv $CATALINA_HOME/webapps.dist $CATALINA_HOME/webapps

COPY ./config/tomcat-users.xml $CATALINA_HOME/conf/

COPY ./config/host-manager_context.xml $CATALINA_HOME/webapps/docs/META-INF/context.xml
COPY ./config/host-manager_context.xml $CATALINA_HOME/webapps/examples/META-INF/context.xml
COPY ./config/host-manager_context.xml $CATALINA_HOME/webapps/host-manager/META-INF/context.xml
COPY ./config/host-manager_context.xml $CATALINA_HOME/webapps/manager/META-INF/context.xml
COPY ./config/host-manager_context.xml $CATALINA_HOME/webapps/ROOT/META-INF/context.xml

# MySQL Connector
WORKDIR $CATALINA_HOME/lib
RUN wget "https://cdn.mysql.com/Downloads/Connector-J/mysql-connector-j-8.4.0.tar.gz" \
    && tar -zxvf mysql-connector-j-8.4.0.tar.gz \
    && mv mysql-connector-j-8.4.0/mysql-connector-j-8.4.0.jar . \
    && rm mysql-connector-j-8.4.0.tar.gz \
    && rm -rf mysql-connector-j-8.4.0

COPY ./copy_resources.sh /usr/local/bin/copy_resources.sh
RUN chmod +x /usr/local/bin/copy_resources.sh

# Expose debugging port

EXPOSE 5005