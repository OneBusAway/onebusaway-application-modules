package org.onebusaway.transit_data_federation.model.predictions;

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
@Table(name = "transit_data_trip_time_predictions")
@org.hibernate.annotations.Entity(mutable = false)
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class TripTimePrediction {

  @Id
  @GeneratedValue
  private final int id = 0;

  @Embedded
  @AttributeOverrides( {
    @AttributeOverride(name = "agencyId", column = @Column(name = "trip_agencyId", length = 50)),
    @AttributeOverride(name = "id", column = @Column(name = "trip_id"))})
  private final AgencyAndId tripId;

  private final long serviceDate;

  private final long time;

  private final int scheduleDeviation;

  public TripTimePrediction(AgencyAndId tripId, long serviceDate, long time,
      int scheduleDeviation) {
    this.tripId = tripId;
    this.serviceDate = serviceDate;
    this.time = time;
    this.scheduleDeviation = scheduleDeviation;
  }
  
  public TripTimePrediction() {
    tripId = null;
    serviceDate = 0;
    time = 0;
    scheduleDeviation = 0;
  }

  public int getId() {
    return id;
  }

  public AgencyAndId getTripId() {
    return tripId;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public long getTime() {
    return time;
  }

  public int getScheduleDeviation() {
    return scheduleDeviation;
  }

  public boolean equivalent(TripTimePrediction prediction) {
    return this.tripId.equals(prediction.getTripId())
        && this.scheduleDeviation == prediction.getServiceDate()
        && this.time == prediction.getTime()
        && this.scheduleDeviation == prediction.getScheduleDeviation();
  }
}
