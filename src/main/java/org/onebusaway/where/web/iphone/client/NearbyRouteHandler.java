/**
 * 
 */
package org.onebusaway.where.web.iphone.client;

import org.onebusaway.common.web.common.client.model.RouteBean;
import org.onebusaway.common.web.common.client.model.StopBean;
import org.onebusaway.common.web.common.client.widgets.DivPanel;
import org.onebusaway.common.web.common.client.widgets.DivWidget;
import org.onebusaway.where.web.common.client.model.NearbyRoutesBean;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

class NearbyRouteHandler implements AsyncCallback<NearbyRoutesBean> {

  private RootPanel _panel;

  private StopPanelHandler _stopPanelHandler = new StopPanelHandler();

  public NearbyRouteHandler(RootPanel panel) {
    _panel = panel;
  }

  public void onSuccess(NearbyRoutesBean bean) {

    Element element = _panel.getElement();
    while (element.getFirstChild() != null)
      element.removeChild(element.getFirstChild());
    _panel.clear();

    List<RouteBean> routes = bean.getRoutes();

    int columns = 4;
    int rows = routes.size() / columns + (routes.size() % columns != 0 ? 1 : 0);

    for (int row = 0; row < rows; row++) {

      Grid grid = new Grid(1, columns);
      grid.addStyleName("nearbyRoutesGrid");

      DivPanel stopPanel = new DivPanel();
      stopPanel.setVisible(false);

      for (int col = 0; col < columns; col++) {
        int index = row * columns + col;
        if (index >= routes.size())
          break;

        RouteBean route = routes.get(index);
        DivWidget widget = new DivWidget(route.getNumber());

        List<StopBean> stops = bean.getNearbyStopsForRoute(route);
        widget.addClickListener(new ClickHandler(route, stops, stopPanel, col));
        widget.addStyleName("nearbyRouteEntry");
        grid.setWidget(0, col, widget);

      }

      _panel.add(grid);
      _panel.add(stopPanel);
    }
  }

  public void onFailure(Throwable ex) {
    System.err.println("error");
    ex.printStackTrace();
  }

  private class ClickHandler implements ClickListener {

    private RouteBean _route;

    private List<StopBean> _stops;

    private DivPanel _stopPanel;

    private int _column;

    public ClickHandler(RouteBean route, List<StopBean> stops, DivPanel stopPanel, int column) {
      _route = route;
      _stops = stops;
      _stopPanel = stopPanel;
      _column = column;
    }

    public void onClick(Widget arg0) {
      _stopPanelHandler.activate(_route, _stops, _stopPanel, _column);
    }
  }

  private class StopPanelHandler {

    private DivPanel _previousPanel = null;

    public void activate(RouteBean route, List<StopBean> stops, DivPanel panel, int column) {

      if (_previousPanel != null) {
        _previousPanel.clear();
        _previousPanel.setVisible(false);
      }

      panel.clear();

      DivWidget arrow = new DivWidget("");
      arrow.addStyleName("nearbyStopsForRouteArrow");
      arrow.addStyleName("nearbyStopsForRouteArrow" + column);
      panel.add(arrow);

      DivPanel buttonPanel = new DivPanel();
      buttonPanel.addStyleName("buttons");
      buttonPanel.addStyleName("nearbyStopsForRouteButtons");

      for (StopBean stop : stops) {
        Anchor stopWidget = new Anchor("<div>" + stop.getName() + "</div><div class=\"arrivalsStopNumber\">Stop # "
            + stop.getId() + " - " + stop.getDirection() + " bound</div>", true, "stop.action?id=" + stop.getId());
        stopWidget.addStyleName("button");
        stopWidget.addStyleName("nearbyStopsForRouteButton");
        buttonPanel.add(stopWidget);
      }

      panel.add(buttonPanel);
      panel.setVisible(true);
      _previousPanel = panel;
    }
  }
}