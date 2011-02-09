package org.onebusaway.webapp.gwt.oba_application.control;

import org.onebusaway.webapp.gwt.oba_application.control.state.State;

public class StateEvent {

  private State _state;

  public StateEvent(State state){
    _state = state;
  }
  
  public State getState() {
    return _state;
  }
}
