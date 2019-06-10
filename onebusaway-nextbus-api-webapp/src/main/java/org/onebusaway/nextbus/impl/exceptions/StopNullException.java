package org.onebusaway.nextbus.impl.exceptions;

import org.onebusaway.nextbus.validation.ErrorMsg;

public class StopNullException extends Exception{
    private static final long serialVersionUID = 1L;

    public StopNullException(){
        super(ErrorMsg.STOP_S_NULL.getDescription());
    }
}
