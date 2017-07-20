package org.onebusaway.nextbus.impl.gtfsrt;

import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.GtfsRealtimeConstants;
import org.onebusaway.gtfs.model.AgencyAndId;

public class GtfsrtHelper {

    public GtfsRealtime.FeedMessage.Builder createFeedWithDefaultHeader() {
        GtfsRealtime.FeedMessage.Builder feedMessage = GtfsRealtime.FeedMessage.newBuilder();

        GtfsRealtime.FeedHeader.Builder feedHeader = GtfsRealtime.FeedHeader.newBuilder();
        feedHeader.setIncrementality(GtfsRealtime.FeedHeader.Incrementality.FULL_DATASET);
        feedHeader.setGtfsRealtimeVersion(GtfsRealtimeConstants.VERSION);
        feedMessage.setHeader(feedHeader);
        return feedMessage;
    }

    public String id(String agencyId, String id) {
        return AgencyAndId.convertToString(new AgencyAndId(agencyId, id));
    }

}
