package org.onebusaway.kcmetro2gtfs.model;

import org.onebusaway.gtfs.csv.schema.FlattenFieldMappingFactory;
import org.onebusaway.gtfs.csv.schema.annotations.CsvField;
import org.onebusaway.gtfs.csv.schema.annotations.CsvFields;
import org.onebusaway.gtfs.model.IdentityBean;

import java.util.Set;

import javax.persistence.EmbeddedId;

@CsvFields(filename = "trips.csv")
public class MetroKCTrip extends IdentityBean<ServicePatternKey> {

  private static final long serialVersionUID = 1L;

  private int tripId;

  @EmbeddedId
  @CsvField(mapping=FlattenFieldMappingFactory.class)
  private ServicePatternKey servicePattern;

  @CsvField(optional = true)
  private String exceptionCode;

  private String scheduleType;

  private String directionName;

  @CsvField(ignore = true)
  private Set<String> changeDates;

  @Override
  public ServicePatternKey getId() {
    return new ServicePatternKey(servicePattern.getChangeDate(), tripId);
  }

  @Override
  public void setId(ServicePatternKey id) {
    throw new UnsupportedOperationException();
  }

  public int getTripId() {
    return tripId;
  }

  public void setTripId(int tripId) {
    this.tripId = tripId;
  }

  public ServicePatternKey getServicePattern() {
    return servicePattern;
  }

  public void setServicePattern(ServicePatternKey servicePattern) {
    this.servicePattern = servicePattern;
  }

  public String getExceptionCode() {
    return exceptionCode;
  }

  public void setExceptionCode(String exceptionCode) {
    this.exceptionCode = exceptionCode;
  }

  public String getScheduleType() {
    return scheduleType;
  }

  public void setScheduleType(String scheduleType) {
    this.scheduleType = scheduleType;
  }

  public String getDirectionName() {
    return directionName;
  }

  public void setDirectionName(String directionName) {
    this.directionName = directionName;
  }

  public Set<String> getChangeDates() {
    return changeDates;
  }

  public void setChangeDates(Set<String> changeDates) {
    this.changeDates = changeDates;
  }

}
