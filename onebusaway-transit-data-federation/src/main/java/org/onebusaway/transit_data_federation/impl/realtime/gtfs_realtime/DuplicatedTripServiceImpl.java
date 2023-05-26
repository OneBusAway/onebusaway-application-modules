package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import com.google.transit.realtime.GtfsRealtime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DuplicatedTripServiceImpl implements DuplicatedTripService{

    private static final Logger _log = LoggerFactory.getLogger(DuplicatedTripServiceImpl.class);

    private DuplicatedTripServiceParser DuplicatedTripServiceParser = new DuplicatedTripServiceParserImpl();
    @Override
    public AddedTripInfo handleDuplicatedDescriptor(GtfsRealtime.TripUpdate tu) {
        return DuplicatedTripServiceParser.parse(tu);
    }
}
