package org.onebusaway.nextbus.model;

import org.onebusaway.gtfs.model.AgencyAndId;

import java.util.Objects;

public class RouteStopId {
    private AgencyAndId routeId;
    private AgencyAndId stopId;

    public RouteStopId(){}
    public RouteStopId(AgencyAndId routeId, AgencyAndId stopId){
        this.routeId = routeId;
        this.stopId = stopId;
    }

    public AgencyAndId getRouteId() {
        return routeId;
    }

    public void setRouteId(AgencyAndId routeId) {
        this.routeId = routeId;
    }

    public AgencyAndId getStopId() {
        return stopId;
    }

    public void setStopId(AgencyAndId stopId) {
        this.stopId = stopId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouteStopId that = (RouteStopId) o;
        return routeId.equals(that.routeId) &&
                stopId.equals(that.stopId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routeId, stopId);
    }
}
