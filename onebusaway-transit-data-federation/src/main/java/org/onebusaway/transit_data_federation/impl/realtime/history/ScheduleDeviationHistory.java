package org.onebusaway.transit_data_federation.impl.realtime.history;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.onebusaway.gtfs.model.AgencyAndId;

@Entity
@Table(name = "oba_schedule_deviation_history")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class ScheduleDeviationHistory implements Serializable {

  private static final long serialVersionUID = 1L;

  @EmbeddedId
  @AttributeOverrides({
      @AttributeOverride(name = "agencyId", column = @Column(name = "trip_agencyId", length = 50)),
      @AttributeOverride(name = "id", column = @Column(name = "trip_id"))})
  private final AgencyAndId tripId;

  @Column(columnDefinition = "longblob")
  private double[] distancesAlongBlock;

  @Column(columnDefinition = "longblob")
  private final double[][] scheduleDeviations;

  public ScheduleDeviationHistory(AgencyAndId tripId,
      double[] distancesAlongBlock, double[][] scheduleDeviations) {
    this.tripId = tripId;
    this.distancesAlongBlock = distancesAlongBlock;
    this.scheduleDeviations = scheduleDeviations;
  }

  ScheduleDeviationHistory() {
    this.tripId = null;
    this.distancesAlongBlock = null;
    this.scheduleDeviations = null;
  }

  public AgencyAndId getTripId() {
    return tripId;
  }

  public int getNumberOfSamples() {
    return scheduleDeviations[0].length;
  }

  public double[] getDistancesAlongBlock() {
    return distancesAlongBlock;
  }

  public double[][] getScheduleDeviations() {
    return scheduleDeviations;
  }
}
