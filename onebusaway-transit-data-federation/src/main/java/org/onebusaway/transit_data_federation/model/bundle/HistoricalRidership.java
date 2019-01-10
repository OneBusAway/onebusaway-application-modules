/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.model.bundle;

import org.onebusaway.gtfs.model.AgencyAndId;

import java.io.Serializable;

/**
 * Historical ridership data index
 */
public class HistoricalRidership implements Serializable {

    public enum CalendarType {
        WEEKDAY,
        SATURDAY,
        SUNDAY
    }

    private AgencyAndId tripId;
    private AgencyAndId routeId;
    private AgencyAndId stopId;
    private CalendarType calendarType;
    private double loadFactor;

    public static Builder builder() { return new Builder(); }

    private HistoricalRidership(Builder builder) {
        this.tripId = builder.tripId;
        this.routeId = builder.routeId;
        this.stopId = builder.stopId;
        this.calendarType = builder.calendarType;
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

    public CalendarType getCalendarType() { return calendarType; }

    public static class Builder {
        private AgencyAndId tripId;
        private AgencyAndId routeId;
        private AgencyAndId stopId;
        private CalendarType calendarType = CalendarType.WEEKDAY;  // DEFAULT
        private double loadFactor;

        public HistoricalRidership create() { return new HistoricalRidership(this); }

        public void setTripId(AgencyAndId tripId) { this.tripId = tripId; }
        public void setRouteId(AgencyAndId routeId) { this.routeId = routeId; }
        public void setStopId(AgencyAndId stopId) { this.stopId = stopId; }
        public void setCalendarType(CalendarType calendarType) { this.calendarType = calendarType; }
        public void setLoadFactor(double loadFactor) { this.loadFactor = loadFactor; }
    }

}
