package org.onebusaway.tripplanner.web.common.client.view;

import org.onebusaway.common.web.common.client.model.ModelListener;
import org.onebusaway.tripplanner.web.common.client.model.DepartureSegmentBean;
import org.onebusaway.tripplanner.web.common.client.model.TripBean;
import org.onebusaway.tripplanner.web.common.client.model.TripPlanModel;
import org.onebusaway.tripplanner.web.common.client.model.TripSegmentBean;
import org.onebusaway.tripplanner.web.common.client.model.WalkSegmentBean;
import org.onebusaway.tripplanner.web.common.client.resources.TripPlannerResources;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.libideas.resources.client.DataResource;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TripPlanResultTablePresenter implements ModelListener<TripPlanModel> {

  private DateTimeFormat _timeFormat = DateTimeFormat.getShortTimeFormat();

  private TripBeanMapPresenter _tripMapPresenter = new TripBeanMapPresenter();

  private FlowPanel _panel = new FlowPanel();

  private MapWidget _map;

  private List<Overlay> _overlays = new ArrayList<Overlay>();

  public TripPlanResultTablePresenter() {
    _panel.addStyleName("TripPlanResultsTable");
  }

  public void setMapWidget(MapWidget map) {
    _map = map;
    _tripMapPresenter.setMapWidget(_map);
  }

  public Widget getWidget() {
    return _panel;
  }

  public void clear() {
    _panel.setVisible(false);
    _panel.clear();
    for (Overlay overlay : _overlays)
      _map.removeOverlay(overlay);
  }

  public void handleUpdate(TripPlanModel model) {

    _panel.clear();
    _panel.setVisible(true);

    List<TripBean> trips = model.getTrips();

    Grid grid = new Grid(trips.size(), 4);
    _panel.add(grid);

    for (int index = 0; index < trips.size(); index++) {
      TripBean trip = trips.get(index);
      Date start = getStartTime(trip);
      Date end = getEndTime(trip);

      String timeStartAndEndLabel = _timeFormat.format(start) + " - " + _timeFormat.format(end);
      String durationLabel = getDurationLable(end.getTime() - start.getTime());
      Widget tripTypeWidget = getTripTypeWidget(trip);

      ClickHandler handler = new ClickHandler(trip);

      Anchor anchor = new Anchor(Integer.toString(index + 1) + ":");
      anchor.addClickListener(handler);

      grid.setWidget(index, 0, anchor);
      grid.setText(index, 1, timeStartAndEndLabel);
      grid.setText(index, 2, durationLabel);
      grid.setWidget(index, 3, tripTypeWidget);

      if (index == 0) {
        displayTrip(trip);
      }
    }
  }

  private Date getStartTime(TripBean trip) {
    TripSegmentBean segment = trip.getSegments().get(0);
    return segment.getTime();
  }

  private Date getEndTime(TripBean trip) {
    List<TripSegmentBean> segments = trip.getSegments();
    TripSegmentBean segment = segments.get(segments.size() - 1);
    return segment.getTime();
  }

  private String getDurationLable(long duration) {

    int minutes = (int) (duration / (1000 * 60));
    int hours = minutes / 60;
    minutes = minutes % 60;

    if (hours == 0 && minutes == 0)
      return "0 mins";

    String label = "";
    if (hours > 0)
      label = Integer.toString(hours) + (hours == 1 ? " hour" : " hours");

    if (minutes == 0 && hours > 0)
      return label;

    if (label.length() > 0)
      label += " ";

    label += Integer.toString(minutes) + (minutes == 1 ? " min" : " mins");

    return label;
  }

  private Widget getTripTypeWidget(TripBean trip) {

    FlowPanel panel = new FlowPanel();
    TripPlannerResources resources = TripPlannerResources.INSTANCE;

    for (TripSegmentBean segment : trip.getSegments()) {
      if (segment instanceof WalkSegmentBean) {
        WalkSegmentBean walk = (WalkSegmentBean) segment;
        if (walk.getLength() > 5280 / 4) {
          DataResource walkIcon = resources.getWalkTripTypeIcon();
          Image image = new Image(walkIcon.getUrl());
          panel.add(image);
        }
      }

      if (segment instanceof DepartureSegmentBean) {
        DataResource busIcon = resources.getBusTripTypeIcon();
        Image image = new Image(busIcon.getUrl());
        panel.add(image);
      }
    }

    return panel;
  }

  private void displayTrip(TripBean trip) {

    for (Overlay overlay : _overlays)
      _map.removeOverlay(overlay);
    _overlays.clear();

    _tripMapPresenter.displayTrip(trip, _overlays);
  }

  public void onFailure(Throwable ex) {
    System.err.println("error in directions handler");
    ex.printStackTrace();
  }

  /*****************************************************************************
   * 
   ****************************************************************************/

  private class ClickHandler implements ClickListener {

    private TripBean _trip;

    public ClickHandler(TripBean trip) {
      _trip = trip;
    }

    public void onClick(Widget arg0) {
      displayTrip(_trip);
    }
  }
}
