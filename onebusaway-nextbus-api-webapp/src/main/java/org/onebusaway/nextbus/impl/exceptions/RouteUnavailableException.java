package org.onebusaway.nextbus.impl.exceptions;

import org.onebusaway.nextbus.validation.ErrorMsg;

public class RouteUnavailableException extends Exception{
    private static final long serialVersionUID = 1L;

    private String agency;

    private String route;

    public RouteUnavailableException(String agency, String route){
        super(ErrorMsg.ROUTE_UNAVAILABLE.getDescription());
        this.agency = agency;
        this.route = route;
    }

    public String getAgency() {
        return agency;
    }

    public String getRoute() {
        return route;
    }
}
