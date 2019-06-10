package org.onebusaway.nextbus.service.validators;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.nextbus.impl.exceptions.StopInvalidException;
import org.onebusaway.nextbus.impl.exceptions.StopNullException;

import java.util.Set;

public interface StopValidator {
    void validate(AgencyAndId stopCode, Set<AgencyAndId> stopIds) throws StopNullException, StopInvalidException;
}
