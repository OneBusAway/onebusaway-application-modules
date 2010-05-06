/**
 * 
 */
package org.onebusaway.kcmetro2gtfs.model;

import org.onebusaway.gtfs.csv.schema.FlattenFieldMappingFactory;
import org.onebusaway.gtfs.csv.schema.annotations.CsvField;
import org.onebusaway.gtfs.csv.schema.annotations.CsvFields;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.kcmetro2gtfs.impl.ServiceTypeFieldMappingFactory;

import org.hibernate.annotations.AccessType;

import javax.persistence.EmbeddedId;

@AccessType("field")
@CsvFields(filename = "service_patterns.csv")
public class MetroKCServicePattern extends IdentityBean<ServicePatternKey> {

  private static final long serialVersionUID = 1L;

  @EmbeddedId
  @AccessType("property")
  @CsvField(mapping=FlattenFieldMappingFactory.class)
  private ServicePatternKey id;

  @CsvField(name = "service_type", mapping = ServiceTypeFieldMappingFactory.class)
  private boolean express;

  private String route;

  private int schedulePatternId;

  private String direction;

  public ServicePatternKey getId() {
    return id;
  }

  public void setId(ServicePatternKey id) {
    this.id = id;
  }

  public boolean isExpress() {
    return express;
  }

  public void setExpress(boolean express) {
    this.express = express;
  }

  public String getRoute() {
    return route;
  }

  public void setRoute(String route) {
    this.route = route;
  }

  public int getSchedulePatternId() {
    return schedulePatternId;
  }

  public void setSchedulePatternId(int schedulePatternId) {
    this.schedulePatternId = schedulePatternId;
  }

  public String getDirection() {
    return direction;
  }

  public void setDirection(String direction) {
    this.direction = direction;
  }

  @Override
  public String toString() {
    return Integer.toString(this.id.getId());
  }
}