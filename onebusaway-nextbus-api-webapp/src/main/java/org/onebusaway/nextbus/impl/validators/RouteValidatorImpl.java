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
import org.onebusaway.nextbus.impl.exceptions.AgencyInvalidException;
import org.onebusaway.nextbus.impl.exceptions.AgencyNullException;
import org.onebusaway.nextbus.impl.exceptions.RouteNullException;
import org.onebusaway.nextbus.impl.exceptions.RouteUnavailableException;
import org.onebusaway.nextbus.service.cache.CacheService;
import org.onebusaway.nextbus.service.cache.TdsCacheService;
import org.onebusaway.nextbus.service.validators.AgencyValidator;
import org.onebusaway.nextbus.service.validators.RouteValidator;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RouteValidatorImpl implements RouteValidator {

    @Autowired
    TransitDataService _transitDataService;

    @Autowired
    TdsCacheService _tdsCache;

    @Autowired
    CacheService _cache;

    @Autowired
    AgencyValidator _agencyValidator;

    @Override
    public void validate(AgencyAndId routeId) throws RouteNullException, RouteUnavailableException {
        if (routeId == null || routeId.getId() == null) {
            throw new RouteNullException(routeId.getAgencyId());
        }
        if (!isValidRoute(routeId)) {
            throw new RouteUnavailableException(routeId.getAgencyId(), routeId.getId());
        }
    }

    @Override
    public boolean isValidRoute(AgencyAndId routeId) {
        if (routeId != null && routeId.hasValues()) {
            // check agencyId
            try {
                _agencyValidator.validate(routeId.getAgencyId());
            } catch(Exception e){
                return false;
            }

            // check route
            final Boolean result = _cache.getRoute(routeId.toString());
            if (result != null) {
                return result;
            }
            if (_transitDataService.getRouteForId(routeId.toString()) != null) {
                _cache.putRoute(routeId.toString(), Boolean.TRUE);
                return true;
            }
            _cache.putRoute(routeId.toString(), Boolean.FALSE);
        }
        return false;
    }
}
