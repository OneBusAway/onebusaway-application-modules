package org.onebusaway.webapp.gwt.viewkit.events;

import org.onebusaway.webapp.gwt.viewkit.ViewController;

import com.google.gwt.event.shared.GwtEvent;

public class ViewControllerSelectedEvent extends GwtEvent<ViewControllerSelectedHandler> {
  
  public static final Type<ViewControllerSelectedHandler> TYPE = new Type<ViewControllerSelectedHandler>();

  private ViewController _viewController;
  
  private int _index;

  public ViewControllerSelectedEvent(ViewController viewController, int index) {
    _viewController = viewController;
    _index = index;
  }
  
  public ViewController getViewController() {
    return _viewController;
  }
  
  public int getIndex() {
    return _index;
  }
  
  @Override
  protected void dispatch(ViewControllerSelectedHandler handler) {
    handler.handleViewControllerSelected(this);
  }

  @Override
  public final com.google.gwt.event.shared.GwtEvent.Type<ViewControllerSelectedHandler> getAssociatedType() {
    return TYPE;
  }
}
