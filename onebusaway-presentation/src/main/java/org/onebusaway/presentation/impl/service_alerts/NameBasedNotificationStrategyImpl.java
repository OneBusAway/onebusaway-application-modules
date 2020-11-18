/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.presentation.impl.service_alerts;

import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
/**
 * Given ids for a resource, look up the appropriate name from the TDS.
 *
 * Performs no caching.
 */
public class NameBasedNotificationStrategyImpl implements NotificationStrategy {
    private static Logger _log = LoggerFactory.getLogger(NameBasedNotificationStrategyImpl.class);

    @Autowired
    private TransitDataService _tds;

    @Override
    public String summarizeRoute(String routeIdStr) {
        if (routeIdStr == null) return null;
        // return the route short name for the give routeId
        try {
            AgencyAndId routeId = AgencyAndIdLibrary.convertFromString(routeIdStr);
            RouteBean route = _tds.getRouteForId(routeIdStr);
            if (route == null || route.getShortName() == null) return routeIdStr;
            return route.getShortName();
        } catch (IllegalStateException ise) {
            // invalid id -- return it as is
            return routeIdStr;
        }

    }

    @Override
    public String summarizeStop(String stopIdStr) {
        // return the stop name
        try {
            AgencyAndId routeId = AgencyAndIdLibrary.convertFromString(stopIdStr);
            StopBean stop = _tds.getStop(routeId.toString());
            if (stop == null || stop.getName() == null) return stopIdStr;
            return stop.getName();
        } catch (IllegalStateException ise) {
            // invalid id -- return it as is
            return stopIdStr;
        } catch (NoSuchStopServiceException nsse) {
            // something went wrong
            _log.error("couldn't find stop for stopId=|" + stopIdStr + "|");
            return stopIdStr;
        }
    }
}
