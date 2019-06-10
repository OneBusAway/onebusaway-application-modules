package org.onebusaway.nextbus.impl.exceptions;

import org.onebusaway.nextbus.validation.ErrorMsg;

public class StopInvalidException extends Exception{
    private static final long serialVersionUID = 1L;

    private String stop;

    public StopInvalidException(String stop){
        super(ErrorMsg.STOP_INVALID.getDescription());
        this.stop =stop;
    }

    public String getStop() {
        return stop;
    }
}
