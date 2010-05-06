package org.onebusaway.oba.web.standard.client.control;

import org.onebusaway.oba.web.standard.client.control.state.State;

public class StateEvent {

  private State _state;

  public StateEvent(State state){
    _state = state;
  }
  
  public State getState() {
    return _state;
  }
}
