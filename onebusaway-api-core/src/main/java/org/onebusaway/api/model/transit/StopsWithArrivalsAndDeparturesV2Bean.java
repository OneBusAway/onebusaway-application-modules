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

import java.io.Serializable;
import java.util.List;

public class StopsWithArrivalsAndDeparturesV2Bean implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<String> stopIds;

    private List<ArrivalAndDepartureV2Bean> arrivalsAndDepartures;

    private List<StopWithDistance> nearbyStopIds;

    private List<String> situationIds;

    private boolean limitExceeded = false;

    public List<String> getStopIds() {
        return stopIds;
    }

    public void setStopIds(List<String> stopIds) {
        this.stopIds = stopIds;
    }

    public List<ArrivalAndDepartureV2Bean> getArrivalsAndDepartures() {
        return arrivalsAndDepartures;
    }

    public void setArrivalsAndDepartures(List<ArrivalAndDepartureV2Bean> arrivalsAndDepartures) {
        this.arrivalsAndDepartures = arrivalsAndDepartures;
    }

    public List<StopWithDistance> getNearbyStopIds() {
        return nearbyStopIds;
    }

    public void setNearbyStopIds(List<StopWithDistance> nearbyStopIds) {
        this.nearbyStopIds = nearbyStopIds;
    }

    public List<String> getSituationIds() {
        return situationIds;
    }

    public void setSituationIds(List<String> situationIds) {
        this.situationIds = situationIds;
    }

    public boolean isLimitExceeded() {
        return limitExceeded;
    }

    public void setLimitExceeded(boolean limitExceeded) {
        this.limitExceeded = limitExceeded;
    }
}
