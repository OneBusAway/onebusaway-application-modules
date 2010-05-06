package org.onebusaway.webapp.gwt.where_refineview;

import org.onebusaway.presentation.client.RoutePresenter;
import org.onebusaway.transit_data.model.RouteBean;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class RouteWidget extends Composite {

  interface MyUiBinder extends UiBinder<Widget, RouteWidget> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  CheckBox selection;

  @UiField
  SpanElement label;

  private RouteBean _route;

  private RouteSelectionHandler _handler;

  public RouteWidget(RouteBean route, boolean selected,
      RouteSelectionHandler handler) {
    _route = route;
    _handler = handler;
    initWidget(uiBinder.createAndBindUi(this));
    selection.setValue(selected);
    label.setInnerText(RoutePresenter.getNameForRoute(route) + " - "
        + RoutePresenter.getDescriptionForRoute(route));
  }

  public boolean isSelected() {
    return selection.getValue();
  }

  @UiHandler("selection")
  void handleClick(ClickEvent e) {
    if (_handler != null)
      _handler.handleSelectionChanged(this, _route, selection.getValue());
  }

  public interface RouteSelectionHandler {
    public void handleSelectionChanged(RouteWidget widget, RouteBean route,
        boolean selected);
  }
}
