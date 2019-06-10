package org.onebusaway.nextbus.impl.exceptions;

import org.onebusaway.nextbus.validation.ErrorMsg;

public class AgencyInvalidException extends Exception {

    private static final long serialVersionUID = 1L;

    private String agencyId;

    public AgencyInvalidException(String agencyId){
        super(ErrorMsg.AGENCY_INVALID.getDescription());
        this.agencyId = agencyId;
    }

    public String getAgencyId() {
        return agencyId;
    }
}
