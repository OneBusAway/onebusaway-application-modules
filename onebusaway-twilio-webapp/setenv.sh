#!/usr/bin/env bash

sed -i 's@${AZURE_COGNITIVE_SERVICE_URL}@'"$AZURE_COGNITIVE_SERVICE_URL"'@g' /oba/tomcat/webapps/onebusaway-twilio-webapp/WEB-INF/classes/data-sources.xml
sed -i 's@${TRANSIT_DATA_SERVICE_URL}@'"$TRANSIT_DATA_SERVICE_URL"'@g' /oba/tomcat/webapps/onebusaway-twilio-webapp/WEB-INF/classes/data-sources.xml
sed -i 's/${DATABASE_HOST}/'"$DATABASE_HOST"'/g' /oba/tomcat/webapps/onebusaway-twilio-webapp/WEB-INF/classes/data-sources.xml
sed -i 's/${DATABASE_PORT}/'"$DATABASE_PORT"'/g' /oba/tomcat/webapps/onebusaway-twilio-webapp/WEB-INF/classes/data-sources.xml
sed -i 's/${DATABASE_NAME}/'"$DATABASE_NAME"'/g' /oba/tomcat/webapps/onebusaway-twilio-webapp/WEB-INF/classes/data-sources.xml
sed -i 's/${DATABASE_USER}/'"$DATABASE_USER"'/g' /oba/tomcat/webapps/onebusaway-twilio-webapp/WEB-INF/classes/data-sources.xml
sed -i 's/${DATABASE_PASSWORD}/'"$DATABASE_PASSWORD"'/g' /oba/tomcat/webapps/onebusaway-twilio-webapp/WEB-INF/classes/data-sources.xml
