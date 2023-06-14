/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.services.transit_graph.dynamic;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.transit_data_federation.services.transit_graph.*;

import java.io.Serializable;
import java.util.List;

public class DynamicTripEntryImpl implements TripEntry, Serializable {

    private static final long serialVersionUID = 7L;

    private AgencyAndId _id;

    private DynamicRouteEntry _route;

    private String _directionId;

    private DynamicBlockEntry _block;

    private LocalizedServiceId _serviceId;

    private AgencyAndId _shapeId;

    private List<StopTimeEntry> _stopTimes;

    private double _totalTripDistance;

    private FrequencyEntry _frequencyLabel;

    public DynamicTripEntryImpl setId(AgencyAndId id) {
        _id = id;
        return this;
    }

    public DynamicTripEntryImpl setRoute(DynamicRouteEntry route) {
        _route = route;
        return this;
    }

    public DynamicTripEntryImpl setDirectionId(String directionId) {
        _directionId = directionId;
        return this;
    }

    public TripEntry setBlock(BlockEntry block) {
        _block = (DynamicBlockEntry) block;
        return this;
    }

    public DynamicTripEntryImpl setServiceId(LocalizedServiceId serviceId) {
        _serviceId = serviceId;
        return this;
    }

    public void setShapeId(AgencyAndId shapeId) {
        _shapeId = shapeId;
    }

    public void setStopTimes(List<StopTimeEntry> stopTimes) {
        _stopTimes = stopTimes;
    }

    public void setTotalTripDistance(double totalTripDistance) {
        _totalTripDistance = totalTripDistance;
    }

    public void setFrequencyLabel(FrequencyEntry frequencyLabel) {
        _frequencyLabel = frequencyLabel;
    }

    /****
     * {@link TripEntry} Interface
     ****/

    @Override
    public AgencyAndId getId() {
        return _id;
    }

    @Override
    public RouteEntry getRoute() {
        return _route;
    }

    @Override
    public RouteCollectionEntry getRouteCollection() {
        return _route.getParent();
    }

    @Override
    public String getDirectionId() {
        return _directionId;
    }

    @Override
    public DynamicBlockEntry getBlock() {
        return _block;
    }

    @Override
    public LocalizedServiceId getServiceId() {
        return _serviceId;
    }

    @Override
    public AgencyAndId getShapeId() {
        return _shapeId;
    }

    @Override
    public List<StopTimeEntry> getStopTimes() {
        return _stopTimes;
    }

    @Override
    public double getTotalTripDistance() {
        return _totalTripDistance;
    }

    @Override
    public FrequencyEntry getFrequencyLabel() {
        return _frequencyLabel;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TripEntry))
            return false;
        // static or dynamic routes are treated the same
        TripEntry trip = (TripEntry) obj;
        return _id.equals(trip.getId())
                && _serviceId.equals(trip.getServiceId());
    }
    @Override
    public int hashCode() {
        return _id.hashCode() + _serviceId.hashCode();
    }
    @Override
    public String toString() {
        return "Trip(" + _id + ")";
    }


}
