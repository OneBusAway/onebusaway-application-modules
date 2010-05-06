package org.onebusaway.where.web.common.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TripStatusBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String status;

  private String tripId;

  private String routeId;

  private String routeName;

  private String destination;

  private int numberOfPredictions;

  private int goalDeviation;

  private List<StopAndTimeBean> _stopsWithTime = new ArrayList<StopAndTimeBean>();

  private String _previousTripId;

  private String _previousRouteName;

  private String _nextTripId;

  private String _nextRouteName;

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public String getRouteName() {
    return routeName;
  }

  public void setRouteName(String routeName) {
    this.routeName = routeName;
  }

  public String getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public int getNumberOfPredictions() {
    return numberOfPredictions;
  }

  public void setNumberOfPredictions(int numberOfPredictions) {
    this.numberOfPredictions = numberOfPredictions;
  }

  public int getGoalDeviation() {
    return goalDeviation;
  }

  public void setGoalDeviation(int goalDeviation) {
    this.goalDeviation = goalDeviation;
  }

  public void addStopAndTimeBean(StopAndTimeBean stopBean) {
    _stopsWithTime.add(stopBean);
  }

  public List<StopAndTimeBean> getStopsWithTime() {
    return _stopsWithTime;
  }

  public void setPreviousTripId(String id) {
    _previousTripId = id;
  }

  public String getPreviousTripId() {
    return _previousTripId;
  }

  public void setPreviousRouteName(String routeName) {
    _previousRouteName = routeName;
  }

  public String getPreviousRouteName() {
    return _previousRouteName;
  }

  public void setNextTripId(String id) {
    _nextTripId = id;
  }

  public String getNextTripId() {
    return _nextTripId;
  }

  public void setNextRouteName(String routeName) {
    _nextRouteName = routeName;
  }

  public String getNextRouteName() {
    return _nextRouteName;
  }
}
