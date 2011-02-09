package org.onebusaway.webapp.gwt.tripplanner_library.view;

import org.onebusaway.transit_data.model.tripplanner.ArrivalSegmentBean;
import org.onebusaway.transit_data.model.tripplanner.DepartureSegmentBean;
import org.onebusaway.transit_data.model.tripplanner.TripPlanBean;
import org.onebusaway.transit_data.model.tripplanner.TripSegmentBean;
import org.onebusaway.transit_data.model.tripplanner.WalkSegmentBean;
import org.onebusaway.webapp.gwt.common.widgets.DivPanel;
import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.tripplanner_library.resources.TripPlannerCssResource;
import org.onebusaway.webapp.gwt.tripplanner_library.resources.TripPlannerResources;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import java.util.Date;
import java.util.List;

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

  public void displayTrip(TripPlanBean trip, int index, String destination) {

    _panel.clear();
    _panel.setVisible(true);

    List<TripSegmentBean> segments = trip.getSegments();
    TripPlannerResources resources = TripPlannerResources.INSTANCE;

    DivPanel summaryPanel = new DivPanel(_css.tripPanelSummaryPanel());
    _panel.add(summaryPanel);

    String duration = TripBeanSupport.getDurationLabel(trip);
    summaryPanel.add(new DivWidget("Showing <span>Trip " + index + "</span>",
        _css.tripPanelSummaryTrip()));
    summaryPanel.add(new DivWidget("Travel time: about " + duration,
        _css.tripPanelSummaryDuration()));

    for (int i = 0; i < segments.size(); i++) {
      TripSegmentBean segment = segments.get(i);

      if (segment instanceof DepartureSegmentBean) {

        DepartureSegmentBean departure = (DepartureSegmentBean) segment;
        ArrivalSegmentBean arrival = getNextArrival(segments, i);

        // if (arrival == null)

        DivPanel panel = new DivPanel();
        panel.addStyleName(_css.tripPanelVehiclePanel());

        DivPanel row1 = new DivPanel(_css.tripPanelVehiclePanelRow1());
        panel.add(row1);

        DataResource busIcon = resources.getBusTripTypeIcon();
        Image image = new Image(busIcon.getUrl());
        image.addStyleName(_css.tripPanelVehiclePanelModeImage());
        row1.add(image);

        int minutes = Math.round((arrival.getTime() - departure.getTime())
            / (1000 * 60));
        row1.add(new DivWidget("Bus - " + departure.getRouteShortName() + " - "
            + departure.getTripHeadsign(), _css.tripPanelVehiclePanelTitle()));

        String departureTime = _timeFormat.format(new Date(departure.getTime()));
        String arrivalTime = _timeFormat.format(new Date(arrival.getTime()));

        DivPanel row2 = new DivPanel(_css.tripPanelVehiclePanelRow2());
        panel.add(row2);

        DivPanel row3 = new DivPanel(_css.tripPanelVehiclePanelRow3());
        panel.add(row3);

        DivPanel row4 = new DivPanel(_css.tripPanelVehiclePanelRow4());
        panel.add(row4);

        row2.add(new DivWidget(departureTime,
            _css.tripPlanVehiclePanelDepartureTime(),
            _css.tripPlanVehiclePanelA()));
        row2.add(new DivWidget("Depart " + departure.getStop().getName(),
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
        row4.add(new DivWidget("Arrive " + arrival.getStop().getName(),
            _css.tripPlanVehiclePanelArrivalTime(),
            _css.tripPlanVehiclePanelB()));
        _panel.add(panel);

      } else if (segment instanceof WalkSegmentBean) {

        WalkSegmentBean walk = (WalkSegmentBean) segment;

        DivPanel panel = new DivPanel();
        panel.addStyleName(_css.tripPanelWalkPanel());

        DivPanel row1 = new DivPanel(_css.tripPanelWalkPanelRow1());
        panel.add(row1);

        DataResource walkIcon = resources.getWalkTripTypeIcon();
        Image image = new Image(walkIcon.getUrl());
        image.addStyleName(_css.tripPanelWalkPanelModeImage());
        row1.add(image);

        String target = getWalkToTarget(segments, i, destination);
        row1.add(new DivWidget("Walk to " + target,
            _css.tripPanelWalkPanelTitle()));

        DivPanel row2 = new DivPanel(_css.tripPanelWalkPanelRow2());
        panel.add(row2);

        row2.add(new DivWidget("About "
            + TripBeanSupport.getDurationLabel(walk.getDuration())));
        _panel.add(panel);
      }
    }
  }

  private ArrivalSegmentBean getNextArrival(List<TripSegmentBean> segments,
      int i) {
    for (; i < segments.size(); i++) {
      TripSegmentBean segment = segments.get(i);
      if (segment instanceof ArrivalSegmentBean) {
        return (ArrivalSegmentBean) segment;
      }
    }

    return null;
  }

  private String getWalkToTarget(List<TripSegmentBean> segments, int i,
      String finalDestination) {
    for (; i < segments.size(); i++) {
      TripSegmentBean segment = segments.get(i);
      if (segment instanceof DepartureSegmentBean) {
        DepartureSegmentBean departure = (DepartureSegmentBean) segment;
        return departure.getStop().getName();
      }
    }

    return finalDestination;
  }
}
