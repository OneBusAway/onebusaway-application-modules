package org.onebusaway.webapp.gwt.oba_application.view;

import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.oba_application.control.StateEvent;
import org.onebusaway.webapp.gwt.oba_application.control.StateEventListener;
import org.onebusaway.webapp.gwt.oba_application.control.TransitScoreControl;
import org.onebusaway.webapp.gwt.oba_application.control.state.SearchProgressState;
import org.onebusaway.webapp.gwt.oba_application.control.state.State;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class TransitScorePresenter {

  private FlowPanel _panel = new FlowPanel();

  private TransitScoreControl _control;

  public TransitScorePresenter() {
    _panel.setVisible(false);
  }

  public void setTransitScoreControl(TransitScoreControl control) {
    _control = control;
  }

  public Widget getWidget() {
    return _panel;
  }

  public StateEventListener getStateEventListener() {
    return new StateEventHandler();
  }

  private class StateEventHandler implements StateEventListener {

    public void handleUpdate(StateEvent model) {
      State state = model.getState();

      if (state instanceof SearchProgressState) {
        double score = _control.getCurrentTransitScore();
        int s = (int) (score * 100);
        _panel.clear();
        _panel.add(new DivWidget("Your transit score: " + s));
        _panel.setVisible(true);
      }
    }
  }

}
