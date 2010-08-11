package org.onebusaway.webapp.gwt.where_library.stop_info_widget;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.presentation.client.RoutePresenter;
import org.onebusaway.presentation.client.StopPresenter;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.common.widgets.SpanWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class StopInfoWidget extends Composite {

  private static MyUiBinder _uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  DivElement _stopName;

  @UiField
  DivElement _stopDescription;
  
  @UiField
  FlowPanel _stopLinks;

  @UiField
  Anchor _realtimeLink;
  
  @UiField
  Anchor _scheduleLink;

  @UiField
  FlowPanel _routesPanel;

  @UiField
  StopInfoWidgetCssResource style;

  private StopInfoWidgetHandler _handler;

  public StopInfoWidget(StopInfoWidgetHandler handler,
      StopBean stop) {
    _handler = handler;

    initWidget(_uiBinder.createAndBindUi(this));

    handleTitleForStop(stop);
    handleLinksForStopInfoWindow(stop);
    handleRoutesForStopInfoWindow(stop);
  }

  protected void handleTitleForStop(StopBean stop) {

    String name = stop.getName();
    _stopName.setInnerText(name);

    String description = "Stop # " + StopPresenter.getCodeForStop(stop);

    if (stop.getDirection() != null)
      description += " - " + stop.getDirection() + " bound";

    _stopDescription.setInnerText(description);
  }

  protected void handleLinksForStopInfoWindow(StopBean bean) {

    _realtimeLink.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent arg0) {
        _handler.handleRealTimeLinkClicked();
      }
    });
    
    _scheduleLink.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent arg0) {
        _handler.handleScheduleLinkClicked();
      }
    });
  }

  protected FlowPanel getStopLinksPanel() {
    return _stopLinks;
  }

  protected void handleRoutesForStopInfoWindow(StopBean stop) {

    List<RouteBean> routes = stop.getRoutes();

    if (routes.isEmpty())
      return;

    List<RouteBean> shortNameRoutes = new ArrayList<RouteBean>();
    List<RouteBean> longNameRoutes = new ArrayList<RouteBean>();

    for (RouteBean route : routes) {
      String name = RoutePresenter.getNameForRoute(route);
      if (name.length() > 3)
        longNameRoutes.add(route);
      else
        shortNameRoutes.add(route);
    }

    if (!shortNameRoutes.isEmpty()) {

      FlowPanel shortNameRoutesPanel = new FlowPanel();
      shortNameRoutesPanel.addStyleName(style.stopInfoWindowRoutesSubPanel());
      _routesPanel.add(shortNameRoutesPanel);

      for (final RouteBean route : shortNameRoutes) {
        SpanWidget w = new SpanWidget(RoutePresenter.getNameForRoute(route));
        w.addStyleName(style.stopInfoWindowRouteShortNameEntry());
        w.addClickHandler(new ClickHandler() {
          public void onClick(ClickEvent arg0) {
            _handler.handleRouteClicked(route);
          }
        });
        shortNameRoutesPanel.add(w);
      }
    }

    if (!longNameRoutes.isEmpty()) {

      FlowPanel longNameRoutesPanel = new FlowPanel();
      longNameRoutesPanel.addStyleName(style.stopInfoWindowRoutesSubPanel());
      _routesPanel.add(longNameRoutesPanel);

      for (final RouteBean route : longNameRoutes) {
        String name = RoutePresenter.getNameForRoute(route);
        DivWidget w = new DivWidget(name);
        w.addStyleName(style.stopInfoWindowRouteLongNameEntry());
        if (name.length() > 10)
          w.addStyleName(style.stopInfoWindowRouteReallyLongNameEntry());
        w.addClickHandler(new ClickHandler() {
          public void onClick(ClickEvent arg0) {
            _handler.handleRouteClicked(route);
          }
        });
        longNameRoutesPanel.add(w);
      }
    }
  }

  interface MyUiBinder extends UiBinder<Widget, StopInfoWidget> {
  }
}
