/**
 * 
 */
package org.onebusaway.kcmetro2gtfs.model;

import org.onebusaway.gtfs.csv.schema.annotations.CsvField;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class ServicePatternKey implements Comparable<ServicePatternKey>,
    Serializable {

  private static final long serialVersionUID = 1L;

  private String changeDate;

  @CsvField(name = "service_pattern_id")
  private int id;

  public ServicePatternKey() {

  }

  public ServicePatternKey(String changeDate, int id) {
    this.changeDate = changeDate;
    this.id = id;
  }

  public String getChangeDate() {
    return this.changeDate;
  }

  public void setChangeDate(String changeDate) {
    this.changeDate = changeDate;
  }

  public int getId() {
    return this.id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int compareTo(ServicePatternKey o) {
    if (this.changeDate == o.changeDate)
      return this.id == o.id ? 0 : (this.id < o.id ? -1 : 1);
    return this.changeDate.compareTo(o.changeDate);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof ServicePatternKey))
      return false;
    ServicePatternKey o = (ServicePatternKey) obj;
    return this.changeDate.equals(o.changeDate) && this.id == o.id;
  }

  @Override
  public int hashCode() {
    return 7 * this.changeDate.hashCode() + 13 * this.id;
  }

  @Override
  public String toString() {
    return "[" + this.changeDate + " id=" + this.id + "]";
  }
}