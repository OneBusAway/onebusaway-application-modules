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
package org.onebusaway.webapp.gwt.agencies_map;

import java.util.List;

import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.webapp.gwt.common.widgets.DivPanel;
import org.onebusaway.webapp.gwt.where_library.rpc.WebappServiceAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.maps.client.InfoWindow;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.MapTypeControl;
import com.google.gwt.maps.client.control.ScaleControl;
import com.google.gwt.maps.client.control.SmallMapControl;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.RootPanel;

public class AgenciesMap implements EntryPoint {

  private static AgencyMapResources _resources = GWT.create(AgencyMapResources.class);
  
  private static AgencyMapCssResource _css = _resources.getCss();

  @Override
  public void onModuleLoad() {

    RootPanel panel = RootPanel.get("agencies_map");
    if (panel == null) {
      System.out.println("you didn't include a div with the id of \"agencies_map\"");
      return;
    }

    MapWidget map = new MapWidget();
    map.addControl(new SmallMapControl());
    map.addControl(new MapTypeControl());
    map.addControl(new ScaleControl());
    map.setScrollWheelZoomEnabled(true);
    panel.add(map);
    
    StyleInjector.inject(_css.getText());

    WebappServiceAsync service = WebappServiceAsync.SERVICE;
    service.getAgencies(new AgencyHandler(map));    
  }

  private static class AgencyHandler implements
      AsyncCallback<List<AgencyWithCoverageBean>> {

    private MapWidget _map;

    public AgencyHandler(MapWidget map) {
      _map = map;
    }

    @Override
    public void onSuccess(List<AgencyWithCoverageBean> agencies) {

      LatLngBounds bounds = LatLngBounds.newInstance();
      for (AgencyWithCoverageBean agencyWithCoverage : agencies) {

        final AgencyBean agency = agencyWithCoverage.getAgency();
        final LatLng point = LatLng.newInstance(agencyWithCoverage.getLat(),
            agencyWithCoverage.getLon());
        bounds.extend(point);

        Marker marker = new Marker(point);
        marker.addMarkerClickHandler(new MarkerClickHandler() {
          @Override
          public void onClick(MarkerClickEvent event) {
            InfoWindow window = _map.getInfoWindow();
            DivPanel panel = new DivPanel();

            DivPanel rowA = new DivPanel(_css.paragraph());
            panel.add(rowA);
            rowA.add(new Anchor(agency.getName(), agency.getUrl()));

            DivPanel rowB = new DivPanel(_css.paragraph());
            panel.add(rowB);
            rowB.add(new Anchor("jump to map", "index.html#m(location)lat("
                + point.getLatitude() + ")lon(" + point.getLongitude()
                + ")accuracy(4)"));
            window.open(point, new InfoWindowContent(panel));
          }
        });

        _map.addOverlay(marker);
      }

      if (!bounds.isEmpty())
        _map.setCenter(bounds.getCenter(), _map.getBoundsZoomLevel(bounds)-1);
    }

    @Override
    public void onFailure(Throwable arg0) {
      // TODO Auto-generated method stub

    }
  }
}
