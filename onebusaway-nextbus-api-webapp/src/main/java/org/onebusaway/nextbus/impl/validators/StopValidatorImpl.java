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
package org.onebusaway.nextbus.impl.validators;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.nextbus.impl.exceptions.StopInvalidException;
import org.onebusaway.nextbus.impl.exceptions.StopNullException;
import org.onebusaway.nextbus.service.cache.TdsCacheService;
import org.onebusaway.nextbus.service.validators.StopValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class StopValidatorImpl implements StopValidator {
    TdsCacheService _cache;

    @Autowired
    public void setTdsCacheService(TdsCacheService cache){
        _cache = cache;
    }

    @Override
    public void validate(AgencyAndId stopCode, Set<AgencyAndId> stopIds) throws StopNullException, StopInvalidException {
        if (stopCode == null || stopCode.getId() == null) {
            throw new StopNullException();
        }
        for(AgencyAndId stopId : stopIds){
            if(_cache.getCachedStopBean(stopId.toString()) != null){
                return;
            }
        }
        throw new StopInvalidException(stopCode.getId());
    }
}
