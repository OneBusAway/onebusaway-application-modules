package org.onebusaway.webapp.gwt.oba_application.view;

import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.oba_application.control.StateEvent;
import org.onebusaway.webapp.gwt.oba_application.control.StateEventListener;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class TransitScoreWelcomePagePresenter {
  private FlowPanel _panel = new FlowPanel();

  public TransitScoreWelcomePagePresenter() {
    _panel.add(new DivWidget(
        "<p>We want to take the hassle out of finding places to eat, see, and go in your neighborhood using mass-transit.</p><p>We've written a tool that makes it easy to search for restaurants, parks, shops, or anything else you might be looking for that are just one bus ride away.</p>"));
  }

  public StateEventListener getStateEventListener() {
    return new StateEventHandler();
  }

  public Widget getWidget() {
    return _panel;
  }

  private class StateEventHandler implements StateEventListener {
    public void handleUpdate(StateEvent model) {
      _panel.setVisible(false);
    }
  }
}
