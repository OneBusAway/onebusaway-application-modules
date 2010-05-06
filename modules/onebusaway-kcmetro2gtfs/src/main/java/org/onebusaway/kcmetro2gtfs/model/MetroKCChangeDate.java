package org.onebusaway.kcmetro2gtfs.model;

import org.onebusaway.gtfs.csv.schema.annotations.CsvField;
import org.onebusaway.gtfs.csv.schema.annotations.CsvFields;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.kcmetro2gtfs.impl.MetroKCDateFieldMappingFactory;

import java.util.Date;

@CsvFields(filename = "change_dates.csv")
public class MetroKCChangeDate extends IdentityBean<String> implements
    Comparable<MetroKCChangeDate> {

  private static final long serialVersionUID = 1L;

  @CsvField(optional = true)
  private String id;

  @CsvField(optional = true, mapping = MetroKCDateFieldMappingFactory.class)
  private Date startDate;

  @CsvField(optional = true, mapping = MetroKCDateFieldMappingFactory.class)
  private Date endDate;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public int compareTo(MetroKCChangeDate o) {
    return id.compareTo(o.id);
  }
}
