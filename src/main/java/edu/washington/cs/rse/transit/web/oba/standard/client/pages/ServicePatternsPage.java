/*
 * Copyright 2008 Brian Ferris
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.washington.cs.rse.transit.web.oba.standard.client.pages;

import java.util.List;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.washington.cs.rse.transit.web.oba.common.client.AbstractPageSource;
import edu.washington.cs.rse.transit.web.oba.common.client.Context;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternPathBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopsBean;

public class ServicePatternsPage extends AbstractPageSource {

    public Widget create(final Context context) {

        VerticalPanel panel = new VerticalPanel();

        String routeNumberV = context.getParam("route");
        int routeNumber = Integer.parseInt(routeNumberV);

        panel.add(new HTML("<h1>Service Patterns</h1>"));

        VerticalPanel patterns = new VerticalPanel();
        panel.add(patterns);

        MapWidget map = new MapWidget();
        map.setSize("500px", "300px");
        map.addControl(new LargeMapControl());
        panel.add(map);

        _service.getActiveServicePatternsByRoute(routeNumber, new ServicePatternsHandler(patterns, map));

        return panel;
    }

    private class ServicePatternsHandler implements AsyncCallback<List<ServicePatternBean>> {

        private VerticalPanel _panel;

        private StopsHandler _handler;

        private PathHandler _pathHandler;

        public ServicePatternsHandler(VerticalPanel panel, MapWidget map) {
            _panel = panel;
            _handler = new StopsHandler(map);
            _pathHandler = new PathHandler(map);
        }

        public void onSuccess(List<ServicePatternBean> beans) {

            for (final ServicePatternBean bean : beans) {
                HTML html = new HTML(bean.getId() + " - " + bean.getGeneralDestination());
                _panel.add(html);
                html.addClickListener(new ClickListener() {
                    public void onClick(Widget arg0) {
                        _service.getActiveStopsByServicePattern(bean.getId(), _handler);
                        _service.getServicePatternPath(bean.getId(), _pathHandler);
                    }
                });
            }
        }

        public void onFailure(Throwable ex) {
            handleException(ex);
        }

    }

    private class StopsHandler implements AsyncCallback<StopsBean> {

        private MapWidget _map;

        public StopsHandler(MapWidget map) {
            _map = map;
        }

        public void onSuccess(StopsBean bean) {

            _map.clearOverlays();

            LatLngBounds bounds = new LatLngBounds();

            List<StopBean> stops = bean.getStops();

            for (final StopBean stop : stops) {
                LatLng p = new LatLng(stop.getLat(), stop.getLon());
                bounds = bounds.extend(p);
                MarkerOptions opts = new MarkerOptions();
                Icon icon = StopIconFactory.getIconForDirection(stop.getDirection(), false);
                opts.setIcon(icon);
                final Marker marker = new Marker(p, opts);
                _map.addOverlay(marker);
            }

            int zoomLevel = _map.getBoundsZoomLevel(bounds);
            _map.setCenter(bounds.getCenter(), zoomLevel);
        }

        public void onFailure(Throwable ex) {
            handleException(ex);
        }
    }

    private class PathHandler implements AsyncCallback<ServicePatternPathBean> {

        private MapWidget _map;

        public PathHandler(MapWidget map) {
            _map = map;
        }

        public void onFailure(Throwable ex) {
            handleException(ex);
        }

        public void onSuccess(ServicePatternPathBean bean) {

            double[] lat = bean.getLat();
            double[] lon = bean.getLon();

            if (lat.length == 0)
                return;

            LatLng[] points = new LatLng[lat.length];

            LatLngBounds bounds = new LatLngBounds();
            for (int i = 0; i < lat.length; i++) {
                points[i] = new LatLng(lat[i], lon[i]);
                bounds = bounds.extend(points[i]);
            }

            _map.addOverlay(new Polyline(points, "#4F64BA", 3, 1.0));

        }

    }
}
