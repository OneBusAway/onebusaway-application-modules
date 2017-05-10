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

import org.apache.commons.lang.StringUtils;
import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsolidatedStopsModificationStrategy implements StopModificationStrategy {

    private FederatedTransitDataBundle _bundle;

    private String _agencyId;

    private Map<AgencyAndId, AgencyAndId> _map;

    @Autowired
    public void setBundle(FederatedTransitDataBundle bundle) {
       _bundle = bundle;
    }

    @Autowired
    public void setAgencyId(String agencyId) {
        _agencyId = agencyId;
    }

    @Refreshable(dependsOn = RefreshableResources.STOP_CONSOLIDATION_FILE)
    public void init() throws IOException {
        if(_bundle.getPath() != null)
            _map = createMap(_bundle.getStopConsolidationFile());
    }

    @Override
    public String convertStopId(String stopId) {
        AgencyAndId id = new AgencyAndId(_agencyId, stopId);
        AgencyAndId newId = _map.get(id);
        if (newId == null)
            return stopId;
        return newId.getId();
    }

    private Map<AgencyAndId, AgencyAndId> createMap(File file) throws IOException {

        FileInputStream in = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        Map<AgencyAndId, AgencyAndId> map = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0 || line.startsWith("#")
                    || line.startsWith("{{{") || line.startsWith("}}}"))
                continue;
            String[] tokens;
            if (line.contains("\"")) {
                tokens = line.split("\"");
            } else {
                tokens = line.split("\\s+");
            }
            List<AgencyAndId> ids = new ArrayList<AgencyAndId>();
            for (String token : tokens) {
                if (StringUtils.isNotBlank(token))
                    ids.add(AgencyAndIdLibrary.convertFromString(token));
            }
            for (int i = 1; i < ids.size(); i++)
                map.put(ids.get(i), ids.get(0));
        }
        return map;
    }
}