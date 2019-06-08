/**
 * Copyright (C) 2019 Cambridge Systematics
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
