package org.onebusaway.nextbus.impl.exceptions;

import org.onebusaway.nextbus.validation.ErrorMsg;

public class RouteNullException extends Exception{
    private static final long serialVersionUID = 1L;

    private String agencyId;

    public RouteNullException(String agencyId){
        super(ErrorMsg.ROUTE_NULL.getDescription());
        this.agencyId = agencyId;
    }

    public String getAgencyId() {
        return agencyId;
    }
}
