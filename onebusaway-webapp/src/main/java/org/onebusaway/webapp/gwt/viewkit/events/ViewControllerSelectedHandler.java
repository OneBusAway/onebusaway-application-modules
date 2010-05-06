package org.onebusaway.webapp.gwt.viewkit.events;

import com.google.gwt.event.shared.EventHandler;

public interface ViewControllerSelectedHandler extends EventHandler {

  public void handleViewControllerSelected(
      ViewControllerSelectedEvent event);

}
