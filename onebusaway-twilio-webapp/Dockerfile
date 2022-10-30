###################################################
# Default Registry Vars
ARG BASE_REGISTRY='docker.io'
ARG BASE_IMAGE=tomcat
ARG BASE_TAG=8.5-jdk11

###################################################
# Registry FROM string
FROM ${BASE_REGISTRY}/${BASE_IMAGE}:${BASE_TAG}

###################################################
# Container metadata
ARG VERSION
ENV DESCRIPTION='OneBusAway Twilio Phone App'

LABEL org.opencontainers.image.title='OneBusAway Twilio Phone App' \
      org.opencontainers.image.description="${DESCRIPTION}" \
      org.opencontainers.image.authors='Nautilus Technology Dev Team <dev@nautilus-tech.com.au>' \
      org.opencontainers.image.source='git@github.com:NautilusTechnologyAU/onebusaway-cognitive-service.git' \
      org.opencontainers.image.url='https://github.com/NautilusTechnologyAU/onebusaway-cognitive-service' \
      org.opencontainers.image.vendor='Nautilus Technology (https://nautilus-tech.com.au)' \
      org.opencontainers.image.licenses='MIT' \
      org.opencontainers.image.version="${VERSION}" \
      help="For more information contact Nautilus Technology Dev Team <dev@nautilus-tech.com.au>"

ENV AZURE_COGNITIVE_SERVICE_URL="http://localhost:8081/v1/text-to-speech"
ENV TRANSIT_DATA_SERVICE_URL="https://localhost:8082/onebusaway-transit-data-federation-webapp/remoting/transit-data-service"
ENV DATABASE_HOST="localhost"
ENV DATABASE_PORT=3306
ENV DATABASE_NAME=oba
ENV DATABASE_USER='oba'
ENV DATABASE_PASSWORD='oba'
ENV JAVA_OPTS="-Xss4m"

# Copy the application files
ADD './target/onebusaway-twilio-webapp.war' /oba/tomcat/webapps/onebusaway-twilio-webapp/onebusaway-twilio-webapp.war
ADD './config.json' /oba/config.json
ADD './setenv.sh' ${CATALINA_HOME}/bin/setenv.sh

WORKDIR /oba/tomcat/webapps/onebusaway-twilio-webapp

# Extract the war file and create symbolic link in Tomcat webapps
RUN jar -xvf onebusaway-twilio-webapp.war && \
    rm onebusaway-twilio-webapp.war && \
    ln -s /oba/tomcat/webapps/onebusaway-twilio-webapp ${CATALINA_HOME}/webapps/ROOT

