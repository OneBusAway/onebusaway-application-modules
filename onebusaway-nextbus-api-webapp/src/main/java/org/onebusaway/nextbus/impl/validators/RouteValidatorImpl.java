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
