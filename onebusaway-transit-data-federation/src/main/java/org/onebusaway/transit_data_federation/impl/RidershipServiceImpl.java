package org.onebusaway.transit_data_federation.impl;

import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.bundle.HistoricalRidership;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.RidershipService;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manage HistoricalRidership Bundle Operations.
 */
@Component
public class RidershipServiceImpl implements RidershipService {
    private static Logger _log = LoggerFactory.getLogger(RidershipServiceImpl.class);

    private FederatedTransitDataBundle _bundle;
    private List<HistoricalRidership> _riderships;
    private Map<AgencyAndId, List<HistoricalRidership>> _tripRiderships;
    private Map<AgencyAndId, List<HistoricalRidership>> _routeRiderships;
    private Map<AgencyAndId, List<HistoricalRidership>> _stopRiderships;
    private Map<String, List<HistoricalRidership>> _tuppleRiderships;

    @Autowired
    public void setBundle(FederatedTransitDataBundle bundle) { _bundle = bundle; }

    @PostConstruct
    @Refreshable(dependsOn = RefreshableResources.TRANSIT_GRAPH)
    public void setup() throws IOException, ClassNotFoundException {
        File path = _bundle.getHistoricalRidershipPath();
        if (path.exists()) {
            _log.info("loading historical riderships...");
            _riderships = ObjectSerializationLibrary.readObject(path);
            _log.info("loading historical riderships...done");
        } else {
            _log.info("historical riderships not found at " + path);
            _riderships = new ArrayList<>();
        }
        _tripRiderships = new HashMap<>();
        _routeRiderships = new HashMap<>();
        _stopRiderships = new HashMap<>();
        _tuppleRiderships = new HashMap<>();

        _log.info("generating historical ridership indexes");
        createIndexes();
        _log.info("complete with " + _riderships.size() + " entries");
    }

    private void createIndexes() {
        for (HistoricalRidership hr : _riderships) {
            AgencyAndId routeId = hr.getRouteId();
            AgencyAndId tripId = hr.getTripId();
            AgencyAndId stopId = hr.getStopId();
            String tupple = tupple = hash(routeId, tripId, stopId);
            if (_routeRiderships.containsKey(routeId)) {
                _routeRiderships.get(routeId).add(hr);
            } else {
                _routeRiderships.put(routeId, createList(hr));
            }

            if (_tripRiderships.containsKey(tripId)) {
                _tripRiderships.get(tripId).add(hr);
            } else {
                _tripRiderships.put(tripId, createList(hr));
            }
            if (_stopRiderships.containsKey(stopId)) {
                _stopRiderships.get(stopId).add(hr);
            } else {
                _stopRiderships.put(stopId, createList(hr));
            }
            if (tupple != null) {
                if ( _tuppleRiderships.containsKey(tupple)) {
                    _tuppleRiderships.get(tupple).add(hr);
                } else {
                    _tuppleRiderships.put(tupple, createList(hr));
                }
            }
        }
    }

    private List<HistoricalRidership> createList(HistoricalRidership hr) {
        List<HistoricalRidership> list = new ArrayList<>();
        list.add(hr);
        return list;
    }

    private String hash(AgencyAndId a1, AgencyAndId a2, AgencyAndId a3) {
        if (a1 != null && a2 != null && a3 != null)
            return a1.toString() + "." + a2.toString() + "." + a3.toString();
        return null;
    }
    @Override
    public List<HistoricalRidership> getAllHistoricalRiderships() {
        return _riderships;
    }

    @Override
    public List<HistoricalRidership> getHistoricalRidershipsForTrip(AgencyAndId tripId) {
        return _tripRiderships.get(tripId);
    }

    @Override
    public List<HistoricalRidership> getHistoricalRidershipsForStop(AgencyAndId stopId) {
        return _stopRiderships.get(stopId);
    }

    @Override
    public List<HistoricalRidership> getHistoricalRidershipsForRoute(AgencyAndId routeId) {
        return _routeRiderships.get(routeId);
    }

    @Override
    public List<HistoricalRidership> getHistoricalRiderships(AgencyAndId routeId, AgencyAndId tripId, AgencyAndId stopId) {
        String hash = hash(routeId, tripId, stopId);
        if (hash == null)
            return  new ArrayList<>();
        return _tuppleRiderships.get(hash);
    }
}
