package org.onebusaway.transit_data_federation.model.predictions;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.onebusaway.gtfs.model.AgencyAndId;

@Entity
@Table(name = "transit_data_trip_time_predictions")
@org.hibernate.annotations.Table(appliesTo = "transit_data_trip_time_predictions", indexes = {@Index(name = "vehicle_and_time", columnNames = {
    "vehicle_agencyId", "vehicle_id","time"})})
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

  @Embedded
  @AttributeOverrides( {
      @AttributeOverride(name = "agencyId", column = @Column(name = "vehicle_agencyId", length = 50)),
      @AttributeOverride(name = "id", column = @Column(name = "vehicle_id"))})
  private final AgencyAndId vehicleId;

  public TripTimePrediction(AgencyAndId tripId, long serviceDate, long time,
      int scheduleDeviation, AgencyAndId vehicleId) {
    this.tripId = tripId;
    this.serviceDate = serviceDate;
    this.time = time;
    this.scheduleDeviation = scheduleDeviation;
    this.vehicleId = vehicleId;
  }

  public TripTimePrediction() {
    tripId = null;
    serviceDate = 0;
    time = 0;
    scheduleDeviation = 0;
    vehicleId = null;
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

  @Index(name="vehicleId")
  public AgencyAndId getVehicleId() {
    return this.vehicleId;
  }

  @Override
  public String toString() {
    return "TripTimePrediction(id=" + id + " tripId=" + tripId
        + " serviceDate=" + serviceDate + " time=" + time
        + " scheduleDeviation=" + scheduleDeviation + " vehicleId=" + vehicleId
        + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + id;
    result = prime * result + scheduleDeviation;
    result = prime * result + (int) (serviceDate ^ (serviceDate >>> 32));
    result = prime * result + (int) (time ^ (time >>> 32));
    result = prime * result + ((tripId == null) ? 0 : tripId.hashCode());
    result = prime * result + ((vehicleId == null) ? 0 : vehicleId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TripTimePrediction other = (TripTimePrediction) obj;
    if (id != other.id)
      return false;
    if (scheduleDeviation != other.scheduleDeviation)
      return false;
    if (serviceDate != other.serviceDate)
      return false;
    if (time != other.time)
      return false;
    if (tripId == null) {
      if (other.tripId != null)
        return false;
    } else if (!tripId.equals(other.tripId))
      return false;
    if (vehicleId == null) {
      if (other.vehicleId != null)
        return false;
    } else if (!vehicleId.equals(other.vehicleId))
      return false;
    return true;
  }
}
