package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import com.google.transit.realtime.GtfsRealtime;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

import java.util.ArrayList;
import java.util.List;

public class DuplicatedTripServiceParserImpl implements DuplicatedTripServiceParser {

    private GtfsRealtimeEntitySource _entitySource;
    @Override
    public AddedTripInfo parse(GtfsRealtime.TripUpdate tu) {
        AddedTripInfo duplicatedTrip = new AddedTripInfo();
        List<AddedStopInfo> stopInfos = new ArrayList<>();
        String tripId = tu.getTrip().getTripId();
        TripEntry tripEntry = _entitySource.getTrip(tripId);

        duplicatedTrip.setAgencyId(tripEntry.getId().getAgencyId());
        duplicatedTrip.setTripStartTime(Integer.parseInt(tu.getTrip().getStartTime()));
        duplicatedTrip.setServiceDate(Long.parseLong(tu.getTrip().getStartDate()));
        duplicatedTrip.setTripId(tripId);
        duplicatedTrip.setRouteId(tripEntry.getRoute().getId().getAgencyId());
        duplicatedTrip.setDirectionId(tripEntry.getDirectionId());
        duplicatedTrip.setShapeId(tripEntry.getShapeId());

        for(StopTimeEntry stopTimeEntry : tripEntry.getStopTimes() ){
            AddedStopInfo stopInfo = new AddedStopInfo();
            stopInfo.setStopId(String.valueOf(stopTimeEntry.getId()));
            stopInfo.setArrivalTime(stopTimeEntry.getArrivalTime());
            stopInfo.setDepartureTime(stopTimeEntry.getDepartureTime());
            stopInfos.add(stopInfo);
        }
        duplicatedTrip.setStops(stopInfos);
    return duplicatedTrip;
    }
}
