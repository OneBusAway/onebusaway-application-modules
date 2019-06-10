package org.onebusaway.nextbus.service.validators;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.nextbus.impl.exceptions.RouteNullException;
import org.onebusaway.nextbus.impl.exceptions.RouteUnavailableException;

public interface RouteValidator {
    void validate(AgencyAndId routeId) throws RouteNullException, RouteUnavailableException;
    boolean isValidRoute(AgencyAndId routeId);
}
