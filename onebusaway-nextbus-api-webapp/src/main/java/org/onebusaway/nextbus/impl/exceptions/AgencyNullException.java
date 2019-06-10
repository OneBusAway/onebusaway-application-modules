package org.onebusaway.nextbus.impl.exceptions;

import org.onebusaway.nextbus.validation.ErrorMsg;

public class AgencyNullException extends Exception{

    private static final long serialVersionUID = 1L;

    public AgencyNullException(){
        super(ErrorMsg.AGENCY_NULL.getDescription());
    }
}
