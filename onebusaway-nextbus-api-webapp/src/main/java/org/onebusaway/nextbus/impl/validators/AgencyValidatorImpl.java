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
