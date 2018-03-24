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
package org.onebusaway.transit_data.model;

import org.onebusaway.gtfs.model.AgencyAndId;

import java.io.Serializable;
import java.util.List;

public class ConsolidatedStopMapBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private AgencyAndId consolidatedStopId;

    private List<AgencyAndId> hiddenStopIds;

    public ConsolidatedStopMapBean(AgencyAndId consolidatedStopId, List<AgencyAndId> hiddenStopIds) {
        this.consolidatedStopId = consolidatedStopId;
        this.hiddenStopIds = hiddenStopIds;
    }

    public AgencyAndId getConsolidatedStopId() {
        return consolidatedStopId;
    }

    public void setConsolidatedStopId(AgencyAndId consolidatedStopId) {
        this.consolidatedStopId = consolidatedStopId;
    }

    public List<AgencyAndId> getHiddenStopIds() {
        return hiddenStopIds;
    }

    public void setHiddenStopIds(List<AgencyAndId> hiddenStopIds) {
        this.hiddenStopIds = hiddenStopIds;
    }
}
