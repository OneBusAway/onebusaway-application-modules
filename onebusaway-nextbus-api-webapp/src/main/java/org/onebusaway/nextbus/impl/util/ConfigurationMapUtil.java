/**
 * Copyright (C) 2019 Cambridge Systematics
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
package org.onebusaway.nextbus.impl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationMapUtil {

    private static Logger _log = LoggerFactory.getLogger(ConfigurationMapUtil.class);

    private Map<String, ConfigurationUtil> _utilMap = new HashMap<>();
    private String defaultAgencyId;

    public void setDefaultAgencyId(String id) {
        defaultAgencyId = id;
    }
    public String getDefaultAgencyId() {
        return defaultAgencyId;
    }

    public void setMap(Map<String, ConfigurationUtil> configMap) {
        _utilMap.clear();
        _utilMap.putAll(configMap);
    }

    public ConfigurationUtil getConfig(String someId) {
        ConfigurationUtil util = getConfigByMappedId(someId);
        if (util != null) {
            _log.info("getMappedIdOrGtfsId (contains) (" + someId + ")=" + util);
            return util;
        }

        util = _utilMap.get(someId);
        _log.info("getMappedIdOrGtfsId (config) (" + someId + ")=" + util);
        return util;
    }

    public ConfigurationUtil getConfigByMappedId(String mappedId) {
        for (ConfigurationUtil util : _utilMap.values()) {
            if (util.getAgencyMapper().containsKey(mappedId)) {
                return util;
            }
        }
        return null;
    }

    public boolean containsId(String gtfsId) {
        return getConfig(gtfsId) != null;
    }

}
