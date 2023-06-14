package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import com.google.transit.realtime.GtfsRealtime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DuplicatedTripServiceImpl implements DuplicatedTripService{

    private static final Logger _log = LoggerFactory.getLogger(DuplicatedTripServiceImpl.class);

    private GtfsRealtimeEntitySource _entitySource;
    public void setGtfsRealtimeEntitySource(GtfsRealtimeEntitySource source) {
        _entitySource = source;
    }

    private DuplicatedTripServiceParser duplicatedTripServiceParser = null;

    @Override
    public AddedTripInfo handleDuplicatedDescriptor(GtfsRealtime.TripUpdate tu) {
        return getParser().parse(tu);
    }

    private DuplicatedTripServiceParser getParser() {
        // we need to lazy load due to the entitySource dependency
        if (duplicatedTripServiceParser == null) {
            duplicatedTripServiceParser = new DuplicatedTripServiceParserImpl();
            duplicatedTripServiceParser.setGtfsRealtimeEntitySource(_entitySource);
        }
        return duplicatedTripServiceParser;
    }
}
