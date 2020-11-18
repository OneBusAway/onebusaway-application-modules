/**
 * Copyright (C) 2017 Cambridge Systematics
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
package org.onebusaway.nextbus.actions.gtfsrt;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.opensymphony.xwork2.ModelDriven;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.nextbus.actions.api.NextBusApiBase;
import org.onebusaway.nextbus.impl.gtfsrt.GtfsrtCache;
import org.onebusaway.nextbus.impl.gtfsrt.GtfsrtHelper;
import org.onebusaway.nextbus.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.onebusaway.nextbus.impl.gtfsrt.GtfsrtCache.ALL_AGENCIES;

public class TripUpdatesAction extends NextBusApiBase  implements
        ModelDriven<FeedMessage> {

    private static Logger _log = LoggerFactory.getLogger(TripUpdatesAction.class);

    @Autowired
    private HttpUtil _httpUtil;

    @Autowired
    private GtfsrtCache _cache;

    private GtfsrtHelper _gtfsrtHelper = new GtfsrtHelper();

    public static final String TRIP_UPDATES_COMMAND = "/command/gtfs-rt/tripUpdates";

    private String agencyId;

    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    public String getAgencyIdHashKey() {
        if (StringUtils.isBlank(agencyId)) {
            return ALL_AGENCIES;
        }
        return agencyId;
    }


    public DefaultHttpHeaders index() {
        return new DefaultHttpHeaders("success");
    }

    @Override
    public FeedMessage getModel() {
        FeedMessage cachedTripUpdates = _cache.getTripUpdates(getAgencyIdHashKey());
        if(cachedTripUpdates != null){
            return cachedTripUpdates;
        }
        else {
            FeedMessage.Builder feedMessage = null; // delay creation until we have a timestamp
            FeedMessage remoteFeedMessage = null;

            List<String> agencyIds = new ArrayList<String>();

            if (agencyId != null) {
                agencyIds.add(agencyId);
            } else {
                Map<String, List<CoordinateBounds>> agencies = _transitDataService.getAgencyIdsWithCoverageArea();
                agencyIds.addAll(agencies.keySet());
            }


            for (String agencyId : agencyIds) {
                if (hasServiceUrl(agencyId)) {
                    String gtfsrtUrl = getServiceUrl(agencyId) + agencyId + TRIP_UPDATES_COMMAND;
                    try {
                        remoteFeedMessage = _httpUtil.getFeedMessage(gtfsrtUrl, 30);
                        if (feedMessage == null) {
                            if (remoteFeedMessage.hasHeader()
                                    && remoteFeedMessage.getHeader().hasTimestamp()
                                    && isTimely(remoteFeedMessage.getHeader().getTimestamp())) {
                                // we set the age of our feed to the age of the first feed that has a timestamp
                                // unless its too old, then we serve the time the response was generated
                                feedMessage = createFeedWithDefaultHeader(remoteFeedMessage.getHeader().getTimestamp());
                            } else {
                                feedMessage = createFeedWithDefaultHeader(null);
                            }
                        }
                        feedMessage.addAllEntity(remoteFeedMessage.getEntityList());
                    } catch (Exception e) {
                        _log.error(e.getMessage());
                        // something went horribly wrong -- serve an empty header in case its a no service period
                        feedMessage = createFeedWithDefaultHeader(null);
                    }
                }
            }
            FeedMessage builtFeedMessage = feedMessage.build();
            _cache.putTripUpdates(getAgencyIdHashKey(), builtFeedMessage);
            return builtFeedMessage;
        }
    }

    public FeedMessage.Builder createFeedWithDefaultHeader(Long timestampInSeconds) {
        return _gtfsrtHelper.createFeedWithDefaultHeader(timestampInSeconds);
    }
}
