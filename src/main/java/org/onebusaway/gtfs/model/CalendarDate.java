package org.onebusaway.gtfs.model;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.onebusaway.common.model.IdentityBean;
import org.onebusaway.csv.CsvField;
import org.onebusaway.csv.CsvFields;
import org.onebusaway.gtfs.serialization.DateFieldMappingFactory;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "gtfs_calendar_dates")
@AccessType("field")
@org.hibernate.annotations.Entity(mutable = false)
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@CsvFields(filename = "calendar_dates.txt")
public class CalendarDate extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  public static final int EXCEPTION_TYPE_ADD = 1;

  public static final int EXCEPTION_TYPE_REMOVE = 2;

  @Id
  @GeneratedValue
  @AccessType("property")
  @CsvField(ignore = true)
  private int id;

  private String serviceId;

  @CsvField(mapping = DateFieldMappingFactory.class)
  private Date date;

  private int exceptionType;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public int getExceptionType() {
    return exceptionType;
  }

  public void setExceptionType(int exceptionType) {
    this.exceptionType = exceptionType;
  }

  @Override
  public String toString() {
    return "CalendarDate(serviceId=" + this.serviceId + " date=" + this.date + " exception=" + this.exceptionType + ")";
  }
}
