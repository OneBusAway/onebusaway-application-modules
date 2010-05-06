/**
 * 
 */
package org.onebusaway.webapp.gwt.viewkit;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;

class ClickableFlowPanel extends FlowPanel {
  public void addClickHandler(ClickHandler handler) {
    addDomHandler(handler, ClickEvent.getType());
  }
}