package org.onebusaway.transit_data_federation.model.bundle;

import org.onebusaway.gtfs.model.AgencyAndId;

import java.io.Serializable;

/**
 * Historical ridership data index
 */
public class HistoricalRidership implements Serializable {
    private AgencyAndId tripId;
    private AgencyAndId routeId;
    private AgencyAndId stopId;
    private double loadFactor;

    public static Builder builder() { return new Builder(); }

    private HistoricalRidership(Builder builder) {
        this.tripId = builder.tripId;
        this.routeId = builder.routeId;
        this.stopId = builder.stopId;
        this.loadFactor = builder.loadFactor;
    }

    public AgencyAndId getStopId() {
        return stopId;
    }

    public double getLoadFactor() {
        return loadFactor;
    }

    public AgencyAndId getRouteId() {
        return routeId;
    }

    public AgencyAndId getTripId() {
        return tripId;
    }

    public static class Builder {
        private AgencyAndId tripId;
        private AgencyAndId routeId;
        private AgencyAndId stopId;
        private double loadFactor;

        public HistoricalRidership create() { return new HistoricalRidership(this); }

        public void setTripId(AgencyAndId tripId) { this.tripId = tripId; }
        public void setRouteId(AgencyAndId routeId) { this.routeId = routeId; }
        public void setStopId(AgencyAndId stopId) { this.stopId = stopId; }
        public void setLoadFactor(double loadFactor) { this.loadFactor = loadFactor; }
    }

}
