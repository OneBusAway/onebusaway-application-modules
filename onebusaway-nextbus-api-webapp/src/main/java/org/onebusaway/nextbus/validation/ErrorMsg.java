/**
 * Copyright (C) 2015 Cambridge Systematics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.nextbus.validation;

public enum ErrorMsg {
  
  AGENCY_NULL("agency parameter \"a\" must be specified in query string"),
  AGENCY_INVALID("Agency parameter \"a=%s\" is not valid."),
  ROUTE_NULL("For agency a=%a route r=null is invalid or temporarily unavailable."),
  ROUTE_INVALID("Could not get route \"%s\" for agency tag \"%s\". One of the tags could be bad."),
  ROUTE_UNAVAILABLE("For agency=%s route r=%s is not currently available. It might be initializing still."),
  ROUTE_ID_NULL("For agency a=%s route r=null is invalid or temporarily unavailable."),
  STOP_INVALID("stopId \"%s\" is not a valid stop id integer"),
  STOP_S_NULL("stop parameter \"s\" must be specified in query string"),
  STOP_STOPS_NULL("must specify \"stops\" parameter in query string"),
  ROUTE_LIMIT("Command would return more routes than the maximum: %01d. Try specifying batches of routes from \"routeList\"."),
  SERVICE_ERROR("Unable to communicate with remote service"),
  DEFAULT_ERROR("No valid results found.");

  private final String description;

  private ErrorMsg(String description) {
    this.description = description;
  }

  public String getDescription() {
     return description;
  }

  @Override
  public String toString() {
    return description;
  }
}
