package org.onebusaway.api.model.transit.tripplanning;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.api.model.transit.FrequencyV2Bean;

public class TransitLegV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String tripId;

  private long serviceDate;

  private String vehicleId;

  private FrequencyV2Bean frequency;

  private String fromStopId;

  private Integer fromStopSequence;

  private String toStopId;

  private Integer toStopSequence;

  private String routeShortName;

  private String routeLongName;

  private String tripHeadsign;

  private String path;

  private long scheduledDepartureTime;

  private long predictedDepartureTime;

  private long scheduledArrivalTime;

  private long predictedArrivalTime;

  private List<String> situationIds;

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }

  public FrequencyV2Bean getFrequency() {
    return frequency;
  }

  public void setFrequency(FrequencyV2Bean frequency) {
    this.frequency = frequency;
  }

  public String getFromStopId() {
    return fromStopId;
  }

  public void setFromStopId(String fromStopId) {
    this.fromStopId = fromStopId;
  }

  public Integer getFromStopSequence() {
    return fromStopSequence;
  }

  public void setFromStopSequence(Integer fromStopSequence) {
    this.fromStopSequence = fromStopSequence;
  }

  public String getToStopId() {
    return toStopId;
  }

  public void setToStopId(String toStopId) {
    this.toStopId = toStopId;
  }

  public Integer getToStopSequence() {
    return toStopSequence;
  }

  public void setToStopSequence(Integer toStopSequence) {
    this.toStopSequence = toStopSequence;
  }

  public String getRouteShortName() {
    return routeShortName;
  }

  public void setRouteShortName(String routeShortName) {
    this.routeShortName = routeShortName;
  }

  public String getRouteLongName() {
    return routeLongName;
  }

  public void setRouteLongName(String routeLongName) {
    this.routeLongName = routeLongName;
  }

  public String getTripHeadsign() {
    return tripHeadsign;
  }

  public void setTripHeadsign(String tripHeadsign) {
    this.tripHeadsign = tripHeadsign;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public long getScheduledDepartureTime() {
    return scheduledDepartureTime;
  }

  public void setScheduledDepartureTime(long scheduledDepartureTime) {
    this.scheduledDepartureTime = scheduledDepartureTime;
  }

  public long getPredictedDepartureTime() {
    return predictedDepartureTime;
  }

  public void setPredictedDepartureTime(long predictedDepartureTime) {
    this.predictedDepartureTime = predictedDepartureTime;
  }

  public long getScheduledArrivalTime() {
    return scheduledArrivalTime;
  }

  public void setScheduledArrivalTime(long scheduledArrivalTime) {
    this.scheduledArrivalTime = scheduledArrivalTime;
  }

  public long getPredictedArrivalTime() {
    return predictedArrivalTime;
  }

  public void setPredictedArrivalTime(long predictedArrivalTime) {
    this.predictedArrivalTime = predictedArrivalTime;
  }

  public List<String> getSituationIds() {
    return situationIds;
  }

  public void setSituationIds(List<String> situationIds) {
    this.situationIds = situationIds;
  }

}
