/**
 * 
 */
package org.onebusaway.metrokc2gtfs.model;

import org.onebusaway.csv.CsvField;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class ServicePatternKey implements Comparable<ServicePatternKey>,
    Serializable {

  private static final long serialVersionUID = 1L;

  private int changeDate;

  @CsvField(name = "service_pattern_id")
  private int id;

  public ServicePatternKey() {

  }

  public ServicePatternKey(int changeDate, int id) {
    this.changeDate = changeDate;
    this.id = id;
  }

  public int getChangeDate() {
    return this.changeDate;
  }

  public void setChangeDate(int changeDate) {
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
    return this.changeDate < o.changeDate ? -1 : 1;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof ServicePatternKey))
      return false;
    ServicePatternKey o = (ServicePatternKey) obj;
    return this.changeDate == o.changeDate && this.id == o.id;
  }

  @Override
  public int hashCode() {
    return 7 * this.changeDate + 13 * this.id;
  }

  @Override
  public String toString() {
    return "ServicePattern(cd=" + this.changeDate + " id=" + this.id + ")";
  }
}