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

import com.google.transit.realtime.GtfsRealtimeConstants;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.FeedHeader.Incrementality;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.model.ResponseBean;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.nextbus.actions.api.NextBusApiBase;
import org.onebusaway.nextbus.actions.api.PredictionsAction;
import org.onebusaway.nextbus.impl.gtfsrt.GtfsrtHelper;
import org.onebusaway.nextbus.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TripUpdatesAction extends NextBusApiBase  implements
        ModelDriven<FeedMessage> {

    private static Logger _log = LoggerFactory.getLogger(TripUpdatesAction.class);

    @Autowired
    private HttpUtil _httpUtil;

    private GtfsrtHelper _gtfsrtHelper = new GtfsrtHelper();

    public static final String TRIP_UPDATES_COMMAND = "/command/gtfs-rt/tripUpdates";

    private String agencyId;

    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    public DefaultHttpHeaders index() {
        return new DefaultHttpHeaders("success");
    }

    @Override
    public FeedMessage getModel() {
    	FeedMessage.Builder feedMessage = createFeedWithDefaultHeader();
    	FeedMessage remoteFeedMessage = null;
    	
    	List<String> agencyIds = new ArrayList<String>();
    	
    	if (agencyId != null) {
            agencyIds.add(agencyId);
    	} else {
	        Map<String,List<CoordinateBounds>> agencies = _transitDataService.getAgencyIdsWithCoverageArea();
	        agencyIds.addAll(agencies.keySet());
    	}
    	
        
    	for(String agencyId : agencyIds){
	        String gtfsrtUrl = getServiceUrl() + agencyId + TRIP_UPDATES_COMMAND;
	        try{
	        	remoteFeedMessage = _httpUtil.getFeedMessage(gtfsrtUrl, 30);
	            feedMessage.addAllEntity(remoteFeedMessage.getEntityList());
	        }
	        catch(Exception e){
	            _log.error(e.getMessage());
	        }
        }

        return feedMessage.build();
    }
    
    public FeedMessage.Builder createFeedWithDefaultHeader() {
        return _gtfsrtHelper.createFeedWithDefaultHeader();
    }
}
