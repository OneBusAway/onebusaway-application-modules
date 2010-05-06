package org.onebusaway.oba.web.standard.client.view;

import org.onebusaway.common.web.common.client.widgets.DivPanel;
import org.onebusaway.common.web.common.client.widgets.DivWidget;
import org.onebusaway.oba.web.standard.client.control.OneBusAwayStandardPresenter;
import org.onebusaway.oba.web.standard.client.control.StateEvent;
import org.onebusaway.oba.web.standard.client.control.StateEventListener;
import org.onebusaway.oba.web.standard.client.control.state.AddressLookupErrorState;
import org.onebusaway.oba.web.standard.client.control.state.State;

import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

public class AddressLookupPresenter {

  private FlowPanel _widget;

  private OneBusAwayStandardPresenter _presenter;

  public void setPresenter(OneBusAwayStandardPresenter presenter) {
    _presenter = presenter;
  }

  public Widget getWidget() {
    if (_widget == null)
      initialize();
    return _widget;

  }

  public StateEventListener getStateEventListener() {
    return new StateEventHandler();
  }

  /*****************************************************************************
   *  
   ****************************************************************************/

  private void initialize() {
    _widget = new FlowPanel();
    _widget.addStyleName("AddressLookup");
    _widget.setVisible(false);
  }

  private class StateEventHandler implements StateEventListener {

    public void handleUpdate(StateEvent event) {
      State state = event.getState();

      if (state instanceof AddressLookupErrorState) {

        AddressLookupErrorState ales = (AddressLookupErrorState) state;
        List<Placemark> locations = ales.getLocations();

        _widget.clear();
        _widget.setVisible(true);

        if (locations.size() == 0) {
          _widget.add(new DivWidget(
              "We couldn't find that adress.  Check to make sure it's correct, or perhaps add the city or zip code?",
              "AddressLookup-NoAddressFound"));
        } else {
          _widget.add(new DivWidget("Did you mean:", "AddressLookup-MultipleAddressesFound"));
          for (final Placemark placemark : locations) {

            DivPanel panel = new DivPanel();
            panel.addStyleName("AddressLookup-Address");
            _widget.add(panel);

            String html = getMarkAsHtml(placemark);
            Anchor anchor = new Anchor(html, true);
            anchor.addClickListener(new ClickListener() {
              public void onClick(Widget arg0) {
                _presenter.setQueryLocation(placemark.getPoint());
              }
            });
            panel.add(anchor);
          }
        }
      } else {
        _widget.setVisible(false);
        _widget.clear();
      }
    }

    private String getMarkAsHtml(Placemark mark) {
      return "<div>" + mark.getStreet() + "</div><div>" + mark.getCity() + ", " + mark.getState() + " "
          + mark.getPostalCode() + "</div>";
    }
  }
}
