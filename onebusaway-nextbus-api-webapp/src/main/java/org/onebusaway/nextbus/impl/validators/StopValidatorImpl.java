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
