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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.ConsolidatedStopsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ConsolidatedStopsModificationStrategy implements StopModificationStrategy {

    private ConsolidatedStopsService _consolidatedStopsService;

    private String _agencyId;

    private static final Logger _log = LoggerFactory.getLogger(ConsolidatedStopsModificationStrategy.class);

    @Autowired
    public void setAgencyId(String agencyId) {
        _agencyId = agencyId;
    }

    @Autowired
    public void setConsolidatedStopsService(ConsolidatedStopsService consolidatedStopsService) {
        _consolidatedStopsService = consolidatedStopsService;
    }

    @Override
    public String convertStopId(String stopId) {
        AgencyAndId id = new AgencyAndId(_agencyId, stopId);
        AgencyAndId newId = _consolidatedStopsService.getConsolidatedStopIdForHiddenStopId(id);
        if (newId == null)
            return stopId;
        return newId.getId();
    }
}
