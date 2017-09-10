/**
 * Copyright (C) 2017 Cambridge Systematics, Inc.
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
package org.onebusaway.rss.model;

import com.rometools.rome.feed.CopyFrom;
import com.rometools.rome.feed.module.ModuleImpl;

/**
 * Bean representing Service Alert Affects Clause as RSS Bean.
 */
public class AffectsClauseRssBean extends ModuleImpl implements IAffectsClause {


    private String agencyId;
    private String routeId;
    private String tripId;
    private String stopId;

    @Override
    public String getAgencyId() { return agencyId; }
    @Override
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }
    @Override
    public String getRouteId() { return routeId; }
    @Override
    public void setRouteId(String routeId) { this.routeId = routeId; }
    @Override
    public String getTripId() { return tripId; }
    @Override
    public void setTripId(String tripId) { this.tripId = tripId; }
    @Override
    public String getStopId() { return stopId; }
    @Override
    public void setStopId(String stopId) { this.stopId = stopId; }

    public AffectsClauseRssBean() {
        super(AffectsClauseRssBean.class, IServiceAlert.URI);
    }
    @Override
    public Class<? extends CopyFrom> getInterface() {
        return AffectsClauseRssBean.class;
    }

    @Override
    public void copyFrom(CopyFrom copyFrom) {
        // not used
    }
}
