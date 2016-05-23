/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data_federation.services;

import java.util.List;

import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsBean;

public interface ScheduleHelperService {

	/**
	 * Check for scheduled service for the given route over the specified time period.  
	 * That is, are any buses scheduled to visit this route in the next time period?
	 * @param agencyId agency of the route
	 * @param time period to check within
	 * @param routeId of route to check
	 * @param directionId of the route to check
	 * @return True if scheduled service is found
	 */
	Boolean routeHasUpcomingScheduledService(String agencyId, long time, String routeId,
			String directionId);

	/**
	 * Check for scheduled service for the given route/stop pairing over the specified
	 * time period.  This is, are any buses scheduled to visit this stop on this route
	 * in the next time period?
	 * @param agencyId agency of the route
	 * @param time period of to check within
	 * @param stopId of the stop to check
	 * @param routeId of the route to check
	 * @param directionId of the route to check
	 * @return True if scheduled service is found
	 */
	Boolean stopHasUpcomingScheduledService(String agencyId, long time, String stopId,
			String routeId, String directionId);

	/**
	 * Given the following partial input, lookup route names that "match".  The
	 * definition of "match" is left to the implementor.  This method can be the
	 * basis for autocompleting text in a user interface.
	 * @param agencyId to constrain the search against; may be null.
	 * @param input partial text representing a GTFS route short name.
	 * @return a list of GTFS short names that may qualify.
	 */
	List<String> getSearchSuggestions(String agencyId, String input);

       /**
        * Given a stop, route, and direction, test if that stop has revenue service
        * on the given route in the given direction.
        * 
        * @param agencyId    Agency ID of stop; used only for routing requests
        *                    to federated backends
        * @param stopId      Agency-and-ID of stop being tested
        * @param routeId     Agency-and-ID of route to filter for
        * @param directionId Direction ID to filter for
        * @return true if the stop being tested ever permits boarding or alighting
        *         from the specified route in the specified direction in the 
        *         currently-loaded bundle; false otherwise
        */        
	Boolean stopHasRevenueServiceOnRoute(String agencyId, String stopId,
		String routeId, String directionId);
        
        /**
         * Given a stop, test if that stop has revenue service.
         * 
         * @param agencyId Agency ID of stop; used only for routing requests
         *                 to federated backends
         * @param stopId   Agency-and-ID of stop being tested
         * @return true if the stop being tested ever permits boarding or alighting
         *         from any route in any direction in the currently-loaded bundle;
         *         false otherwise
         */
	Boolean stopHasRevenueService(String agencyId, String stopId);

	/**
	 * given a list of stops for an agency, filter out the non-revenue stops <i>en masse</i>
	 * Included for performance reasons to batch calls.
	 */
  List<StopBean> filterRevenueService(AgencyBean agency, StopsBean stops);

}
