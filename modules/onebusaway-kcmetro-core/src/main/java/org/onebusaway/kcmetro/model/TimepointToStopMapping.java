package org.onebusaway.kcmetro.model;

import org.onebusaway.container.model.IdentityBean;
import org.onebusaway.gtfs.model.AgencyAndId;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "timepoint_to_stop_mappings")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class TimepointToStopMapping extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue
  private Integer id;

  @Embedded
  @AttributeOverrides( {
      @AttributeOverride(name = "agencyId", column = @Column(name = "trackTripId_agencyId", nullable = false)),
      @AttributeOverride(name = "id", column = @Column(name = "trackerTripId_id", nullable = false))})
  private AgencyAndId trackerTripId;

  @Embedded
  @AttributeOverrides( {
      @AttributeOverride(name = "agencyId", column = @Column(name = "tripId_agencyId", nullable = false)),
      @AttributeOverride(name = "id", column = @Column(name = "tripId_id", nullable = false))})
  private AgencyAndId tripId;

  @Embedded
  @AttributeOverrides( {
      @AttributeOverride(name = "agencyId", column = @Column(name = "serviceId_agencyId", nullable = false)),
      @AttributeOverride(name = "id", column = @Column(name = "serviceId_id", nullable = false))})
  private AgencyAndId serviceId;

  @Embedded
  @AttributeOverrides( {
      @AttributeOverride(name = "agencyId", column = @Column(name = "timepointId_agencyId", nullable = false)),
      @AttributeOverride(name = "id", column = @Column(name = "timepointId_id", nullable = false))})
  private AgencyAndId timepointId;

  @Embedded
  @AttributeOverrides( {
      @AttributeOverride(name = "agencyId", column = @Column(name = "stopId_agencyId", nullable = false)),
      @AttributeOverride(name = "id", column = @Column(name = "stopId_id", nullable = false))})
  private AgencyAndId stopId;
  
  private int time;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public AgencyAndId getTrackerTripId() {
    return trackerTripId;
  }

  public void setTrackerTripId(AgencyAndId trackerTripId) {
    this.trackerTripId = trackerTripId;
  }

  public AgencyAndId getTripId() {
    return tripId;
  }

  public void setTripId(AgencyAndId tripId) {
    this.tripId = tripId;
  }

  public AgencyAndId getServiceId() {
    return serviceId;
  }

  public void setServiceId(AgencyAndId serviceId) {
    this.serviceId = serviceId;
  }

  public AgencyAndId getTimepointId() {
    return timepointId;
  }

  public void setTimepointId(AgencyAndId timepointId) {
    this.timepointId = timepointId;
  }

  public AgencyAndId getStopId() {
    return stopId;
  }

  public void setStopId(AgencyAndId stopId) {
    this.stopId = stopId;
  }

  public int getTime() {
    return time;
  }

  public void setTime(int time) {
    this.time = time;
  }
  
  @Override
  public String toString() {
    return "TimepointToStopMapping(id="+id+" trackerTripId=" + trackerTripId + " tripId=" + tripId + " serviceId=" + serviceId + " timepointId=" + timepointId + " stopId=" + stopId + " time=" + time + ")";
  }
}
