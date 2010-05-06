package org.onebusaway.metrokc2gtdf.model;

import org.onebusaway.csv.CsvField;
import org.onebusaway.csv.CsvFields;
import org.onebusaway.gtdf.model.IdentityBean;

import javax.persistence.EmbeddedId;

@CsvFields(filename = "trips.csv")
public class MetroKCTrip extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  private Integer id;

  @EmbeddedId
  private ServicePatternKey servicePattern;

  @CsvField(optional = true)
  private String exceptionCode;

  private String scheduleType;

  private String directionName;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
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
}
