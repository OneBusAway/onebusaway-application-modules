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

import org.onebusaway.nextbus.impl.exceptions.AgencyInvalidException;
import org.onebusaway.nextbus.impl.exceptions.AgencyNullException;
import org.onebusaway.nextbus.model.nextbus.Body;
import org.onebusaway.nextbus.model.nextbus.BodyError;
import org.onebusaway.nextbus.service.cache.TdsCacheService;
import org.onebusaway.nextbus.service.validators.AgencyValidator;
import org.onebusaway.nextbus.validation.ErrorMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AgencyValidatorImpl implements AgencyValidator {

    TdsCacheService _cache;

    @Autowired
    public void setTdsCacheService(TdsCacheService cache){
        _cache = cache;
    }

    @Override
    public void validate(String agencyId) throws AgencyNullException, AgencyInvalidException {
        if (agencyId == null) {
          throw new AgencyNullException();
        }
        if (_cache.getCachedAgencyBean(agencyId) == null) {
            throw new AgencyInvalidException(agencyId);
        }
    }
}
