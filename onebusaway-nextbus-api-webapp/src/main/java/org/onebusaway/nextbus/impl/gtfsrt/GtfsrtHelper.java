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
package org.onebusaway.nextbus.impl.gtfsrt;

import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.GtfsRealtimeConstants;
import org.onebusaway.gtfs.model.AgencyAndId;

import java.util.Date;

public class GtfsrtHelper {

    public GtfsRealtime.FeedMessage.Builder createFeedWithDefaultHeader(Long timestampInSeconds) {
        GtfsRealtime.FeedMessage.Builder feedMessage = GtfsRealtime.FeedMessage.newBuilder();

        GtfsRealtime.FeedHeader.Builder feedHeader = GtfsRealtime.FeedHeader.newBuilder();
        feedHeader.setIncrementality(GtfsRealtime.FeedHeader.Incrementality.FULL_DATASET);
        feedHeader.setGtfsRealtimeVersion(GtfsRealtimeConstants.VERSION);
        if (timestampInSeconds != null) {
            feedHeader.setTimestamp(timestampInSeconds);
        } else {
            feedHeader.setTimestamp(new Date().getTime()/1000);
        }
        feedMessage.setHeader(feedHeader);
        return feedMessage;
    }

    public String id(String agencyId, String id) {
        return AgencyAndId.convertToString(new AgencyAndId(agencyId, id));
    }

}
