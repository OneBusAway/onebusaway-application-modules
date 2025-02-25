#!/usr/bin/env bash

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


echo "Copying resources to Tomcat for OBA_VERSION $OBA_VERSION"

cp -f /root/.m2/repository/org/onebusaway/onebusaway-api-webapp/$OBA_VERSION/onebusaway-api-webapp-$OBA_VERSION.war /usr/local/tomcat/webapps/onebusaway-api-webapp.war
cp -f /root/.m2/repository/org/onebusaway/onebusaway-transit-data-federation-webapp/$OBA_VERSION/onebusaway-transit-data-federation-webapp-$OBA_VERSION.war /usr/local/tomcat/webapps/onebusaway-transit-data-federation-webapp.war

unzip /usr/local/tomcat/webapps/onebusaway-api-webapp.war -d /usr/local/tomcat/webapps/onebusaway-api-webapp
unzip /usr/local/tomcat/webapps/onebusaway-transit-data-federation-webapp.war -d /usr/local/tomcat/webapps/onebusaway-transit-data-federation-webapp

cp -f /oba_config_files/onebusaway-api-webapp-data-sources.xml /usr/local/tomcat/webapps/onebusaway-api-webapp/WEB-INF/classes/data-sources.xml
cp -f /oba_config_files/onebusaway-transit-data-federation-webapp-data-sources.xml /usr/local/tomcat/webapps/onebusaway-transit-data-federation-webapp/WEB-INF/classes/data-sources.xml
