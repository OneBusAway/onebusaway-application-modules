package org.onebusaway.webapp.gwt.where_library.view.events;

import org.onebusaway.transit_data.model.StopBean;

import com.google.gwt.event.shared.GwtEvent;

public class StopClickedEvent extends GwtEvent<StopClickedHandler> {
  
  public static final Type<StopClickedHandler> TYPE = new Type<StopClickedHandler>();
  
  private StopBean _stop;


  public StopClickedEvent(StopBean stop) {
    _stop = stop;
  }
  
  public StopBean getStop() {
    return _stop;
  }
  
  @Override
  protected void dispatch(StopClickedHandler handler) {
    handler.handleStopClicked(this);
  }

  @Override
  public final com.google.gwt.event.shared.GwtEvent.Type<StopClickedHandler> getAssociatedType() {
    return TYPE;
  }
}
