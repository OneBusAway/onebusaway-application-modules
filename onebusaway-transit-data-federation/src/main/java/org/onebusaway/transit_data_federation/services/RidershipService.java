package org.onebusaway.transit_data_federation.services;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.bundle.HistoricalRidership;

import java.util.List;

public interface RidershipService {
    public List<HistoricalRidership> getAllHistoricalRiderships();
    public List<HistoricalRidership> getHistoricalRidershipsForTrip(AgencyAndId tripId);
    public List<HistoricalRidership> getHistoricalRidershipsForStop(AgencyAndId stopId);
    public List<HistoricalRidership> getHistoricalRidershipsForRoute(AgencyAndId routeId);
    public List<HistoricalRidership> getHistoricalRiderships(AgencyAndId routeId, AgencyAndId tripId, AgencyAndId stopId);

}
