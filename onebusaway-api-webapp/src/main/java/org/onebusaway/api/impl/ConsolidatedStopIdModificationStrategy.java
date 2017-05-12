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
package org.onebusaway.api.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.onebusaway.api.services.AgencyAndIdModificationStrategy;
import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ConsolidatedStopMapBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

public class ConsolidatedStopIdModificationStrategy implements AgencyAndIdModificationStrategy {
    private TransitDataService _transitDataService;

    private Map<Pair<String, AgencyAndId>, AgencyAndId> _map;

    @Autowired
    public void setTransitDataService(TransitDataService transitDataService) {
        _transitDataService = transitDataService;
    }

    @PostConstruct
    @Refreshable(dependsOn = RefreshableResources.BUNDLE_SWAP)
    public void init() {
        _map = new HashMap<Pair<String, AgencyAndId>, AgencyAndId>();
        ListBean<ConsolidatedStopMapBean> beans = _transitDataService.getAllConsolidatedStops();
        for (ConsolidatedStopMapBean bean : beans.getList()) {
            for (AgencyAndId hidden : bean.getHiddenStopIds()) {
                _map.put(Pair.of(hidden.getAgencyId(), bean.getConsolidatedStopId()), hidden);
            }
        }
    }

    @Override
    public AgencyAndId convertId(String targetAgency, AgencyAndId stopId) {
        return _map.get(Pair.of(targetAgency, stopId));
    }

}

