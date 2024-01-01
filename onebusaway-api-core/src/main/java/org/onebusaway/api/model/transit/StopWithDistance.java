/**
 * Copyright (C) 2022 Cambridge Systematics, Inc.
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
package org.onebusaway.api.model.transit;

/**
 * A stop with a distanceFromQuery.  DistanceFromQuery
 * is defined as the distance between center of the bounds specified in the query
 * and the lat/lon of the specified stopId.
 */
public class StopWithDistance {
    private String stopId;
    private Double distanceFromQuery;

    public StopWithDistance(String stopId, Double distanceFromQuery) {
        this.stopId = stopId;
        this.distanceFromQuery = distanceFromQuery;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public Double getDistanceFromQuery() {
        return distanceFromQuery;
    }

    public void setDistanceFromQuery(Double distanceFromQuery) {
        this.distanceFromQuery = distanceFromQuery;
    }
}
