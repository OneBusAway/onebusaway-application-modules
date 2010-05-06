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
/**
 * 
 */
package edu.washington.cs.rse.transit.web.oba.standard.client.pages.constraints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

import edu.washington.cs.rse.transit.web.oba.common.client.Context;
import edu.washington.cs.rse.transit.web.oba.common.client.model.PathBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternPathBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternTimeBlockBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternTimeBlocksBean;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.NoSuchRouteServiceException;
import edu.washington.cs.rse.transit.web.oba.common.client.widgets.DivWidget;
import edu.washington.cs.rse.transit.web.oba.common.client.widgets.SpanWidget;
import edu.washington.cs.rse.transit.web.oba.standard.client.pages.ESearchType;
import edu.washington.cs.rse.transit.web.oba.standard.client.pages.StopIconFactory;

public class RouteConstraint extends AbstractConstraint {

    private static final DateTimeFormat _format = DateTimeFormat.getShortTimeFormat();

    private ServicePatternTimeBlockHandler _blockHandler = new ServicePatternTimeBlockHandler();

    private ServicePatternPathHandler _pathHandler = new ServicePatternPathHandler();

    private int _route;

    private int _servicePatternId = -1;

    private String _scheduleType = null;

    private ServicePatternTimeBlocksBean _timeBlocksCache = null;

    /***************************************************************************
     * Public Methods
     **************************************************************************/

    public RouteConstraint(int route) {
        _route = route;
    }

    public void update(Context context) {

        _wrapper.setSearchText(ESearchType.ROUTE, Integer.toString(_route));

        if (context.hasParam(KEY_SERVICE_PATTERN)) {
            try {
                _servicePatternId = Integer.parseInt(context.getParam(KEY_SERVICE_PATTERN));
            } catch (NumberFormatException ex) {

            }
        }

        _scheduleType = context.getParam(KEY_SCHEDULE_TYPE);
        if (_timeBlocksCache != null)
            _blockHandler.onSuccess(_timeBlocksCache);
        else
            _service.getServicePatternTimeBlocksByRoute(_route, _blockHandler);

        if (_servicePatternId > 0) {
            _service.getServicePatternPath(_servicePatternId, _pathHandler);
            _service.getActiveStopsByServicePattern(_servicePatternId, _stopsHandler);
        }

        if (context.hasParam("link")) {
            try {
                int linkId = Integer.parseInt(context.getParam("link"));
                _service.getTransLinkPath(linkId, new PathHandler());
            } catch (NumberFormatException ex) {

            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof RouteConstraint))
            return false;

        RouteConstraint rc = (RouteConstraint) obj;
        return _route == rc._route;
    }

    @Override
    public int hashCode() {
        return _route;
    }

    /***************************************************************************
     * Internal Classes
     **************************************************************************/

    private class ServicePatternTimeBlockHandler implements AsyncCallback<ServicePatternTimeBlocksBean> {

        public void onSuccess(ServicePatternTimeBlocksBean result) {

            _timeBlocksCache = result;
            _resultsPanel.clear();

            Map<String, List<ServicePatternTimeBlockBean>> byScheduleType = getBeansByScheduleType(result.getBlocks());

            List<String> keys = new ArrayList<String>();
            keys.add("WEEKDAY");
            keys.add("SATURDAY");
            keys.add("SUNDAY");

            FlowPanel routePanel = new FlowPanel();
            routePanel.addStyleName("routePanel");
            _resultsPanel.add(routePanel);

            routePanel.add(new DivWidget("routeNumber", _msgs.standardIndexPageRouteNumber(_route)));
            routePanel.add(new DivWidget("routeSelection", _msgs.standardIndexPageSelectADestination()));

            Tree tree = new Tree();
            tree.addStyleName("routeTree");
            routePanel.add(tree);

            boolean isFirst = true;

            for (final String scheduleType : keys) {

                List<ServicePatternTimeBlockBean> s = byScheduleType.get(scheduleType);

                if (s == null)
                    continue;

                SpanWidget scheduleTypeWidget = new SpanWidget(scheduleType);
                final TreeItem scheduleTypeItem = new TreeItem(scheduleTypeWidget);
                scheduleTypeItem.addStyleName("routeTreeScheduleType");
                tree.addItem(scheduleTypeItem);

                scheduleTypeWidget.addClickListener(new ClickListener() {
                    public void onClick(Widget arg0) {
                        scheduleTypeItem.setState(!scheduleTypeItem.getState());
                    }
                });

                Map<String, List<ServicePatternTimeBlockBean>> byDest = getBeansByDestination(s);

                List<String> dests = new ArrayList<String>(byDest.keySet());
                Collections.sort(dests);

                for (final String dest : dests) {

                    SpanWidget destWidget = new SpanWidget(dest);
                    TreeItem destItem = new TreeItem(destWidget);
                    destItem.addStyleName("routeTreeDestination");
                    scheduleTypeItem.addItem(destItem);

                    List<ServicePatternTimeBlockBean> blocks = byDest.get(dest);
                    Collections.sort(blocks);

                    int maxRange = 0;
                    ServicePatternTimeBlockBean maxBlock = null;

                    for (ServicePatternTimeBlockBean block : blocks) {

                        int range = block.getMaxPassingTime() - block.getMinPassingTime();
                        if (range > maxRange) {
                            maxRange = range;
                            maxBlock = block;
                        }

                        final int id = block.getServicePattern().getId();
                        String from = getPassingTimeAsString(result.getStartOfDay(), block.getMinPassingTime());
                        String to = getPassingTimeAsString(result.getStartOfDay(), block.getMaxPassingTime());

                        String label = from + " to " + to;
                        if (block.getServicePattern().isExpress())
                            label += " EXPRESS";
                        SpanWidget timeWidget = new SpanWidget(label);
                        TreeItem timeItem = new TreeItem(timeWidget);
                        timeItem.addStyleName("routeTreeTime");
                        destItem.addItem(timeItem);

                        if (id == _servicePatternId) {
                            timeWidget.addStyleName("routeTreeTimeSelected");
                            destItem.setState(true);
                        }

                        timeWidget.addClickListener(new ClickListener() {
                            public void onClick(Widget arg0) {
                                newTarget(INDEX_PAGE, KEY_MODE, MODE_ROUTE, KEY_ROUTE, _route, KEY_SERVICE_PATTERN, id,
                                        KEY_SCHEDULE_TYPE, scheduleType);
                            }
                        });
                    }

                    if (maxBlock != null) {
                        final int id = maxBlock.getServicePattern().getId();
                        destWidget.addClickListener(new ClickListener() {
                            public void onClick(Widget arg0) {
                                newTarget(INDEX_PAGE, KEY_MODE, MODE_ROUTE, KEY_ROUTE, _route, KEY_SERVICE_PATTERN, id,
                                        KEY_SCHEDULE_TYPE, scheduleType);
                            }
                        });
                    }
                }

                if ((isFirst && _scheduleType == null) || (_scheduleType != null && _scheduleType.equals(scheduleType))) {
                    isFirst = false;
                    scheduleTypeItem.setState(true);
                }
            }

        }

        public void onFailure(Throwable ex) {
            _resultsPanel.add(new SpanWidget("fail (1)"));
            if (ex instanceof NoSuchRouteServiceException) {
                _resultsPanel.add(new DivWidget("noSuchRouteError", _msgs.commonNoSuchRoute()));
            } else {
                handleException(ex);
            }
        }

        private String getPassingTimeAsString(long startOfDay, int passingTime) {
            long t = startOfDay + passingTime * 60 * 1000;
            return _format.format(new Date(t));
        }

        private Map<String, List<ServicePatternTimeBlockBean>> getBeansByScheduleType(
                Iterable<ServicePatternTimeBlockBean> beans) {

            Map<String, List<ServicePatternTimeBlockBean>> blocksByScheduleType = new HashMap<String, List<ServicePatternTimeBlockBean>>();

            for (ServicePatternTimeBlockBean bean : beans) {
                String st = bean.getScheduleType();
                List<ServicePatternTimeBlockBean> s = blocksByScheduleType.get(st);
                if (s == null) {
                    s = new ArrayList<ServicePatternTimeBlockBean>();
                    blocksByScheduleType.put(st, s);
                }
                s.add(bean);
            }
            return blocksByScheduleType;
        }

        private Map<String, List<ServicePatternTimeBlockBean>> getBeansByDestination(
                Iterable<ServicePatternTimeBlockBean> beans) {

            Map<String, List<ServicePatternTimeBlockBean>> blocksByDestination = new HashMap<String, List<ServicePatternTimeBlockBean>>();

            for (ServicePatternTimeBlockBean bean : beans) {
                String dest = bean.getServicePattern().getGeneralDestination();
                List<ServicePatternTimeBlockBean> s = blocksByDestination.get(dest);
                if (s == null) {
                    s = new ArrayList<ServicePatternTimeBlockBean>();
                    blocksByDestination.put(dest, s);
                }
                s.add(bean);
            }
            return blocksByDestination;

        }

    }

    private class ServicePatternPathHandler implements AsyncCallback<ServicePatternPathBean> {

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

            MarkerOptions startOpts = new MarkerOptions();
            startOpts.setIcon(StopIconFactory.getRouteStartIcon());
            startOpts.setClickable(false);
            _map.addOverlay(new Marker(points[0], startOpts));

            MarkerOptions endOpts = new MarkerOptions();
            endOpts.setIcon(StopIconFactory.getRouteEndIcon());
            endOpts.setClickable(false);
            _map.addOverlay(new Marker(points[points.length - 1], endOpts));

            _map.addOverlay(new Polyline(points, "#4F64BA", 3, 1.0));

            int zoom = _map.getBoundsZoomLevel(bounds);
            _map.setCenter(bounds.getCenter(), zoom);

        }

        public void onFailure(Throwable ex) {
            handleException(ex);
        }
    }

    private class PathHandler implements AsyncCallback<PathBean> {

        public void onSuccess(PathBean bean) {

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

            _map.addOverlay(new Polyline(points, "#FF0000", 5, 1.0));
        }

        public void onFailure(Throwable ex) {
            handleException(ex);
        }
    }

}