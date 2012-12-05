/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.services.schedule;

/**
 * Service that returns the state of scheduled service for a given set of parameters (time, route, direction, etc.)
 * 
 * @author jmaki
 *
 */
public interface ScheduledServiceService {

  public Boolean routeHasUpcomingScheduledService(long time, String routeId, String directionId);

  public Boolean stopHasUpcomingScheduledService(long time, String stopId, String routeId, String directionId);

}