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
package org.onebusaway.transit_data_federation.impl;

import org.apache.commons.lang.StringUtils;
import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ConsolidatedStopMapBean;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.ConsolidatedStopsService;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ConsolidatedStopsServiceImpl implements ConsolidatedStopsService {
    private static Logger _log = LoggerFactory.getLogger(ConsolidatedStopsServiceImpl.class);
    private FederatedTransitDataBundle _bundle;

    private Map<AgencyAndId, ConsolidatedStopMapBean> _index;

    @Autowired
    public void setBundle(FederatedTransitDataBundle bundle) {
        _bundle = bundle;
    }

    @Override
    public Collection<ConsolidatedStopMapBean> getAllConsolidatedStops() {
        if (_index == null) return new ArrayList<ConsolidatedStopMapBean>();
        return _index.values();
    }

    @Override
    public AgencyAndId getConsolidatedStopIdForHiddenStopId(AgencyAndId id) {
        if (_index == null || _index.get(id) == null)
            return null;
        return _index.get(id).getConsolidatedStopId();
    }

    @Refreshable(dependsOn = RefreshableResources.STOP_CONSOLIDATION_FILE)
    public void init() throws IOException {
        File stopFile = _bundle.getStopConsolidationFile();
        if ( stopFile.exists() && stopFile.isFile()) {
            buildIndex(_bundle.getStopConsolidationFile());
        } else {
            _log.warn("missing Stop Consolidation File; skipping");
        }
    }

    private void buildIndex(File file) throws IOException {

        _index = new HashMap<AgencyAndId, ConsolidatedStopMapBean>();

        FileInputStream in = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

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

            List<AgencyAndId> hiddenStops = ids.subList(1, ids.size());
            ConsolidatedStopMapBean bean = new ConsolidatedStopMapBean(ids.get(0), hiddenStops);
            // all agencies for which this consolidated stop map is relevant
            for (AgencyAndId stop : hiddenStops)
                _index.put(stop, bean);
        }
    }
}
