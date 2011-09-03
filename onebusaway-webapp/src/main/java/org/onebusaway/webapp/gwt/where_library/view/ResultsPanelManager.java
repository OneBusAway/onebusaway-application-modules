/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.webapp.gwt.where_library.view;

import java.util.List;

import org.onebusaway.presentation.client.RoutePresenter;
import org.onebusaway.presentation.client.StopPresenter;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.webapp.gwt.common.control.Place;
import org.onebusaway.webapp.gwt.common.widgets.DivPanel;
import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.where_library.rpc.WebappServiceAsync;
import org.onebusaway.webapp.gwt.where_library.services.CombinedSearchResult;
import org.onebusaway.webapp.gwt.where_library.view.constraints.OperationContext;
import org.onebusaway.webapp.gwt.where_library.view.stops.PlaceClickHandler;
import org.onebusaway.webapp.gwt.where_library.view.stops.TransitMapManager;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.InfoWindow;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

public class ResultsPanelManager {

  private static WebappServiceAsync _service = WebappServiceAsync.SERVICE;

  private OperationContext _context;

  private StopFinderCssResource _css;

  private StopFinderInterface _stopFinder;

  private Panel _panel;

  private TransitMapManager _transitMapManager;

  public ResultsPanelManager(OperationContext context) {
    _context = context;
    _css = context.getWidget().getCss();
    _stopFinder = context.getStopFinder();
    _transitMapManager = context.getTransitMapManager();
    _panel = context.getPanel();
  }

  public void addNoResultsMessage() {
    _panel.clear();
    _panel.add(new DivWidget("No results were found for your search",
        _css.resultListWarning()));
  }

  public void setResults(CombinedSearchResult result) {
    _panel.clear();
    addClearSearchLinkToResultsPanel();

    Object primary = getPrimaryResult(result);
    addPrimaryResult(primary);

    if (!result.isEmpty()) {

      DivPanel panel = new DivPanel(_css.resultListAdditional());
      _panel.add(panel);

      panel.add(new DivWidget("Did you mean:", _css.resultListWarning()));

      List<RouteBean> routes = result.getRoutes();
      addElementsToPanel(panel, routes, "Routes");

      List<StopBean> stops = result.getStops();
      addElementsToPanel(panel, stops, "Stops");

      List<Place> addresses = result.getAddresses();
      DivPanel addressesPanel = addElementsToPanel(panel, addresses,
          "Addresses");
      if (!addresses.isEmpty()) {
        ShowPlacesOnMapToggleHandler handler = addShowOnMapLink(addressesPanel,
            addresses, primary);
        if (primary == null && routes.isEmpty() && stops.isEmpty())
          handler.setShowingOnMap(true);
      }

      List<Place> places = result.getPlaces();
      DivPanel placesPanel = addElementsToPanel(panel, places, "Places");
      if (!places.isEmpty()) {
        ShowPlacesOnMapToggleHandler handler = addShowOnMapLink(placesPanel,
            places, primary);
        if (primary == null && routes.isEmpty() && stops.isEmpty()
            && addresses.isEmpty())
          handler.setShowingOnMap(true);
      }
    }
  }

  public void setResult(Object result) {
    _panel.clear();
    addClearSearchLinkToResultsPanel();
    addPrimaryResult(result);
  }

  /****
   * Private Methods
   ****/

  private void addPrimaryResult(Object primary) {
    if (primary != null) {
      DivPanel primaryPanel = new DivPanel(_css.resultListPrimary());
      _panel.add(primaryPanel);
      addResultToPanel(primaryPanel, primary);
      displayPrimaryResult(primary);
    }
  }

  protected void addClearSearchLinkToResultsPanel() {

    Anchor anchor = new Anchor("Clear this search");
    anchor.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent arg0) {
        _stopFinder.queryCurrentView();
      }
    });
    DivPanel p = new DivPanel(_css.resultListClear());
    p.add(anchor);
    _panel.add(p);
  }

  private Object getPrimaryResult(CombinedSearchResult result) {

    List<RouteBean> routes = result.getRoutes();
    List<Place> addresses = result.getAddresses();
    List<StopBean> stops = result.getStops();
    List<Place> places = result.getPlaces();

    if (routes.size() == 1)
      return routes.remove(0);

    if (routes.isEmpty() && addresses.size() == 1)
      return addresses.remove(0);

    if (routes.isEmpty() && addresses.isEmpty() && stops.size() == 1)
      return stops.remove(0);

    if (routes.isEmpty() && addresses.isEmpty() && stops.isEmpty()
        && places.size() == 1)
      return places.remove(0);

    return null;
  }

  private void displayPrimaryResult(Object primary) {
    if (primary instanceof RouteBean) {
      RouteBean route = (RouteBean) primary;
      _service.getStopsForRoute(route.getId(), new StopsForRouteCallback(route));
    } else if (primary instanceof StopBean) {
      StopBean stop = (StopBean) primary;
      _transitMapManager.showStop(stop, true);
    } else if (primary instanceof Place) {
      Place place = (Place) primary;
      _transitMapManager.showPlace(place, true, null);
    }
  }

  private DivPanel addElementsToPanel(DivPanel parentPanel, List<?> objects,
      String label) {

    if (objects.isEmpty())
      return null;

    DivPanel panel = new DivPanel(_css.resultList());
    parentPanel.add(panel);
    panel.add(new DivWidget(label + ":", _css.resultListHeader()));

    for (Object bean : objects)
      addResultToPanel(panel, bean);

    return panel;
  }

  protected void addResultToPanel(DivPanel panel, Object primary) {

    if (primary instanceof RouteBean) {
      addRouteBeanToPanel((RouteBean) primary, panel);
    } else if (primary instanceof StopBean) {
      addStopBeanToPanel((StopBean) primary, panel);
    } else if (primary instanceof Place) {
      addPlaceBeanToPanel((Place) primary, panel);
    }
  }

  private void addRouteBeanToPanel(final RouteBean route, DivPanel panel) {

    DivPanel resultPanel = new DivPanel(_css.resultListEntry());
    panel.add(resultPanel);

    DivPanel routeRow = new DivPanel(_css.resultListEntryName());
    resultPanel.add(routeRow);

    String name = getRouteName(route);
    Anchor anchor = new Anchor(name);
    anchor.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        _stopFinder.queryRoute(route.getId());
      }
    });
    routeRow.add(anchor);

    AgencyBean agency = route.getAgency();
    String agencyLink = agency.getName();
    if (agency.getUrl() != null)
      agencyLink = "<a href=\"" + agency.getUrl() + "\">" + agency.getName()
          + "</a>";
    resultPanel.add(new DivWidget("Operated by " + agencyLink,
        _css.resultListEntryDescription()));
  }

  protected void addStopBeanToPanel(StopBean stop, DivPanel panel) {

    DivPanel resultPanel = new DivPanel(_css.resultListEntry());
    panel.add(resultPanel);

    DivPanel nameRow = new DivPanel(_css.resultListEntryName());
    resultPanel.add(nameRow);

    Anchor anchor = new Anchor(stop.getName(), false,
        _stopFinder.getStopQueryLink(stop.getId()));
    nameRow.add(anchor);

    String desc = "Stop # " + StopPresenter.getCodeForStop(stop);
    if (stop.getDirection() != null)
      desc += " - " + stop.getDirection() + " bound";
    resultPanel.add(new DivWidget(desc, _css.resultListEntryDescription()));
  }

  protected void addPlaceBeanToPanel(final Place place, DivPanel panel) {

    DivPanel resultPanel = new DivPanel(_css.resultListEntry());
    panel.add(resultPanel);

    DivPanel routeRow = new DivPanel(_css.resultListEntryName());
    resultPanel.add(routeRow);

    String name = place.getName();
    Anchor anchor = new Anchor(name);
    anchor.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        _stopFinder.queryLocation(place.getLocation(), place.getAccuracy());
      }
    });
    routeRow.add(anchor);

    String description = place.getDescriptionAsString();
    if (description.length() > 0)
      routeRow.add(new DivWidget(description, _css.resultListEntryDescription()));
  }

  private String getRouteName(RouteBean route) {
    String name = RoutePresenter.getNameForRoute(route);
    String desc = RoutePresenter.getDescriptionForRoute(route);
    if (desc != null)
      name += " - " + desc;
    return name;
  }

  private ShowPlacesOnMapToggleHandler addShowOnMapLink(DivPanel parentPanel,
      final List<Place> addresses, final Object primaryResult) {

    DivPanel row = new DivPanel(_css.resultListMoreInfoLink());
    parentPanel.add(row);

    final Anchor anchor = new Anchor("Show all on map");
    row.add(anchor);

    ShowPlacesOnMapToggleHandler handler = new ShowPlacesOnMapToggleHandler(
        anchor, addresses, primaryResult);
    anchor.addClickHandler(handler);
    return handler;
  }

  private class StopsForRouteCallback implements
      AsyncCallback<StopsForRouteBean> {

    private RouteBean _route;

    public StopsForRouteCallback(RouteBean route) {
      _route = route;
    }

    @Override
    public void onSuccess(StopsForRouteBean stopsForRoute) {

      boolean centerViewOnRoute = !_context.isLocationSet();

      _transitMapManager.showStopsForRoute(_route, stopsForRoute,
          centerViewOnRoute);
    }

    @Override
    public void onFailure(Throwable arg0) {

    }
  }

  private class ShowPlacesOnMapToggleHandler implements ClickHandler,
      PlaceClickHandler {

    private boolean _showing = false;

    private Anchor _anchor;

    private List<Place> _places;

    private Object _primaryResult;

    public ShowPlacesOnMapToggleHandler(Anchor anchor, List<Place> places,
        Object primaryResult) {
      _anchor = anchor;
      _places = places;
      _primaryResult = primaryResult;
    }

    @Override
    public void onClick(ClickEvent arg0) {
      setShowingOnMap(!_showing);
    }

    public void setShowingOnMap(boolean showing) {
      _showing = showing;
      _anchor.setText(_showing ? "Hide all on map" : "Show all on map");

      if (_showing) {
        _transitMapManager.showPlaces(_places, true, this);
      } else {
        if (_primaryResult != null)
          displayPrimaryResult(_primaryResult);
        else
          _transitMapManager.showStopsInCurrentView();
      }
    }

    @Override
    public void onPlaceClicked(final Place place) {

      LatLng p = place.getLocation();
      MapWidget _map = _transitMapManager.getMap();
      InfoWindow window = _map.getInfoWindow();

      FlowPanel panel = new FlowPanel();
      panel.add(new DivWidget(place.getName()));
      String desc = place.getDescriptionAsString();
      if (desc.length() > 0)
        panel.add(new DivWidget(desc));

      DivPanel row = new DivPanel();
      panel.add(row);

      Anchor anchor = new Anchor("Show nearby transit stops");
      row.add(anchor);

      anchor.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent arg0) {
          _transitMapManager.showPlace(place, true,
              ShowPlacesOnMapToggleHandler.this);
        }
      });

      window.open(p, new InfoWindowContent(panel));
    }
  }
}
