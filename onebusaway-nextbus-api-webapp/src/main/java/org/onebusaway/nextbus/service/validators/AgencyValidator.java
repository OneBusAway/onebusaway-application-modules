package org.onebusaway.nextbus.service.validators;

import org.onebusaway.nextbus.impl.exceptions.AgencyInvalidException;
import org.onebusaway.nextbus.impl.exceptions.AgencyNullException;

public interface AgencyValidator {
    void validate(String agencyId) throws AgencyNullException, AgencyInvalidException;
}
