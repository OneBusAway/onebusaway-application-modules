/**
 * Copyright (C) 2017 Metropolitan Transportation Authority
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
package org.onebusaway.realtime.api;

import org.onebusaway.gtfs.model.AgencyAndId;

import java.util.Date;

/**
 * Represents information provided by Automatic Passenger Counter (APC)
 * systems.
 */
public class VehicleOccupancyRecord {

    private AgencyAndId vehicleId;
    private Date timestamp;
    private OccupancyStatus occupancyStatus;
    private String routeId;
    private String directionId;


    public AgencyAndId getVehicleId() {

        return vehicleId;
    }

    public void setVehicleId(AgencyAndId vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public OccupancyStatus getOccupancyStatus() {
        return occupancyStatus;
    }

    public String getRouteId() { return routeId; }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getDirectionId() { return directionId; }

    public void setDirectionId(String directionId) { this.directionId = directionId; }

    public void setOccupancyStatus(OccupancyStatus occupancyStatus) {
        this.occupancyStatus = occupancyStatus;
        if (timestamp == null)
            timestamp = new Date();
    }

    public String toString() {
        return "Occupancy[" + vehicleId + "]{"
                + occupancyStatus + ":" + timestamp
                + "}";
    }
}
