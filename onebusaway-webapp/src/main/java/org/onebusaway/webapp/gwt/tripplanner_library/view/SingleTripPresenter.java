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
package org.onebusaway.webapp.gwt.tripplanner_library.view;

import java.util.Date;
import java.util.List;

import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.tripplanning.ItineraryBean;
import org.onebusaway.transit_data.model.tripplanning.LegBean;
import org.onebusaway.transit_data.model.tripplanning.TransitLegBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.webapp.gwt.common.widgets.DivPanel;
import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.tripplanner_library.resources.TripPlannerCssResource;
import org.onebusaway.webapp.gwt.tripplanner_library.resources.TripPlannerResources;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class SingleTripPresenter {

  private static TripPlannerCssResource _css = TripPlannerResources.INSTANCE.getCss();

  private static DateTimeFormat _timeFormat = DateTimeFormat.getShortTimeFormat();

  private FlowPanel _panel = new FlowPanel();

  public SingleTripPresenter() {
    _panel.setVisible(false);
    _panel.addStyleName(_css.tripPlan());
  }

  public Widget getWidget() {
    return _panel;
  }

  public void displayTrip(ItineraryBean trip, int index, String destination) {

    _panel.clear();
    _panel.setVisible(true);

    List<LegBean> legs = trip.getLegs();
    TripPlannerResources resources = TripPlannerResources.INSTANCE;

    DivPanel summaryPanel = new DivPanel(_css.tripPanelSummaryPanel());
    _panel.add(summaryPanel);

    String duration = TripBeanSupport.getDurationLabel(trip);
    summaryPanel.add(new DivWidget("Showing <span>Trip " + index + "</span>",
        _css.tripPanelSummaryTrip()));
    summaryPanel.add(new DivWidget("Travel time: about " + duration,
        _css.tripPanelSummaryDuration()));

    for (int i = 0; i < legs.size(); i++) {
      LegBean leg = legs.get(i);

      String mode = leg.getMode();
      if( mode.equals("transit")) {

        TransitLegBean transitLeg = leg.getTransitLeg();
        TripBean tripBean = transitLeg.getTrip();
        RouteBean routeBean = tripBean.getRoute();

        DivPanel panel = new DivPanel();
        panel.addStyleName(_css.tripPanelVehiclePanel());

        DivPanel row1 = new DivPanel(_css.tripPanelVehiclePanelRow1());
        panel.add(row1);

        DataResource busIcon = resources.getBusTripTypeIcon();
        Image image = new Image(busIcon.getUrl());
        image.addStyleName(_css.tripPanelVehiclePanelModeImage());
        row1.add(image);

        int minutes = Math.round((leg.getEndTime() - leg.getStartTime())
            / (1000 * 60));
        
        String routeShortName = getBestName(transitLeg.getRouteShortName(),tripBean.getRouteShortName(), routeBean.getShortName(),"");
        String tripHeadsign = getBestName(transitLeg.getTripHeadsign(),tripBean.getTripHeadsign(),routeBean.getLongName(),"");
        
        row1.add(new DivWidget("Bus - " + routeShortName + " - "
            + tripHeadsign, _css.tripPanelVehiclePanelTitle()));

        String departureTime = _timeFormat.format(new Date(leg.getStartTime()));
        String arrivalTime = _timeFormat.format(new Date(leg.getEndTime()));

        DivPanel row2 = new DivPanel(_css.tripPanelVehiclePanelRow2());
        panel.add(row2);

        DivPanel row3 = new DivPanel(_css.tripPanelVehiclePanelRow3());
        panel.add(row3);

        DivPanel row4 = new DivPanel(_css.tripPanelVehiclePanelRow4());
        panel.add(row4);
        
        String fromStopName = "";
        StopBean fromStop = transitLeg.getFromStop();
        if( fromStop != null)
          fromStopName = fromStop.getName();
        
        String toStopName = "";
        StopBean toStop = transitLeg.getToStop();
        if( toStop != null)
          toStopName = toStop.getName();

        row2.add(new DivWidget(departureTime,
            _css.tripPlanVehiclePanelDepartureTime(),
            _css.tripPlanVehiclePanelA()));
        row2.add(new DivWidget("Depart " + fromStopName,
            _css.tripPlanVehiclePanelDepartureLabel(),
            _css.tripPlanVehiclePanelB()));
        row3.add(new DivWidget("...",
            _css.tripPlanVehiclePanelInTransitLabel(),
            _css.tripPlanVehiclePanelA()));
        row3.add(new DivWidget(minutes + " mins",
            _css.tripPlanVehiclePanelInTransitTime(),
            _css.tripPlanVehiclePanelB()));
        row4.add(new DivWidget(arrivalTime,
            _css.tripPlanVehiclePanelArrivalLabel(),
            _css.tripPlanVehiclePanelA()));
        row4.add(new DivWidget("Arrive " + toStopName,
            _css.tripPlanVehiclePanelArrivalTime(),
            _css.tripPlanVehiclePanelB()));
        _panel.add(panel);

      } else if (mode.equals("walk")) {
        
        DivPanel panel = new DivPanel();
        panel.addStyleName(_css.tripPanelWalkPanel());

        DivPanel row1 = new DivPanel(_css.tripPanelWalkPanelRow1());
        panel.add(row1);

        DataResource walkIcon = resources.getWalkTripTypeIcon();
        Image image = new Image(walkIcon.getUrl());
        image.addStyleName(_css.tripPanelWalkPanelModeImage());
        row1.add(image);

        String target = getWalkToTarget(legs, i, destination);
        row1.add(new DivWidget("Walk to " + target,
            _css.tripPanelWalkPanelTitle()));

        DivPanel row2 = new DivPanel(_css.tripPanelWalkPanelRow2());
        panel.add(row2);
        
        long dur = leg.getEndTime() - leg.getStartTime();

        row2.add(new DivWidget("About "
            + TripBeanSupport.getDurationLabel(dur)));
        _panel.add(panel);
      }
    }
  }

  private String getWalkToTarget(List<LegBean> legs, int i,
      String finalDestination) {
    for (; i < legs.size(); i++) {
      LegBean segment = legs.get(i);
      if( segment.getMode().equals("transit")) {
        TransitLegBean transitLeg = segment.getTransitLeg();
        if( transitLeg.getFromStop() != null)
          return transitLeg.getFromStop().getName();
      }
    }

    return finalDestination;
  }
  
  private String getBestName(String... names) {
    for( String name : names) {
      if( name != null && ! name.isEmpty() )
        return name;
    }
    return null;
  }
}
