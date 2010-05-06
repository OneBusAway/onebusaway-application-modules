/**
 * 
 */
package org.onebusaway.metrokc2gtdf.model;

import org.hibernate.annotations.AccessType;
import org.onebusaway.csv.CsvField;
import org.onebusaway.csv.CsvFields;
import org.onebusaway.gtdf.model.IdentityBean;
import org.onebusaway.metrokc2gtdf.ServiceTypeFieldMappingFactory;

import javax.persistence.EmbeddedId;

@AccessType("field")
@CsvFields(filename = "service_patterns.csv")
public class MetroKCServicePattern extends IdentityBean<ServicePatternKey> {

  private static final long serialVersionUID = 1L;

  @EmbeddedId
  @AccessType("property")
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