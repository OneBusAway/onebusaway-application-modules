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
package org.onebusaway.nextbus.impl.cache;

import org.onebusaway.nextbus.impl.util.ConfigurationMapUtil;
import org.onebusaway.nextbus.service.cache.CacheService;
import org.onebusaway.nextbus.service.cache.TdsCacheService;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TdsCacheServiceImpl implements TdsCacheService {

    @Autowired
    protected ConfigurationMapUtil _configMapUtil;

    @Autowired
    protected TransitDataService _transitDataService;

    @Autowired
    private CacheService _cache;

    @Override
    public AgencyBean getCachedAgencyBean(String id) {
        if (_configMapUtil.getConfig(id) != null) {
            AgencyBean bean = _cache.getAgency(id);
            if (bean == null) {
                bean = _transitDataService.getAgency(id);
                if (bean != null)
                    _cache.putAgency(id, bean);
            }
            return bean;
        }
        return null;
    }

    @Override
    public StopBean getCachedStopBean(String id) {
        StopBean stop = _cache.getStop(id);
        if (stop == null) {
            if (_cache.isInvalidStop(id)) {
                return null;
            }

            try {
                stop = _transitDataService.getStop(id);
            } catch (Throwable t) {
                _cache.setInvalidStop(id);
            }
            if (stop != null) {
                _cache.putStop(id, stop);
            } else {
                _cache.setInvalidStop(id);
            }
        }
        return stop;
    }
}
