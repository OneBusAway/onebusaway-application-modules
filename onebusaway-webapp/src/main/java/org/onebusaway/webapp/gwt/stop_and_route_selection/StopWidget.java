package org.onebusaway.webapp.gwt.stop_and_route_selection;

import org.onebusaway.transit_data.model.StopBean;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class StopWidget extends Composite {

  interface MyUiBinder extends UiBinder<Widget, StopWidget> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  SpanElement label;

  @UiField
  Anchor removeStopAnchor;

  private StopBean _stop;

  private RemoveClickedHandler _handler;

  public StopWidget(StopBean stop, RemoveClickedHandler handler) {
    _stop = stop;
    _handler = handler;

    initWidget(uiBinder.createAndBindUi(this));
    String title = stop.getName();
    if( stop.getCode() != null)
      title += " - Stop # " + stop.getCode();
    if( stop.getDirection() != null)
      title += " - " + stop.getDirection() + " bound";
    label.setInnerText(title);
    removeStopAnchor.setHref("#removeStop");
  }

  @UiHandler("removeStopAnchor")
  void handleRemoveStopClick(ClickEvent e) {
    e.preventDefault();
    if (_handler != null)
      _handler.handleRemoveClicked(this, _stop);
  }

  public interface RemoveClickedHandler {
    public void handleRemoveClicked(StopWidget widget, StopBean stop);
  }
}
