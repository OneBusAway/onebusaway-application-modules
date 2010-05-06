package org.onebusaway.gtfs.serialization.mappings;

import org.onebusaway.gtfs.csv.CsvEntityContext;
import org.onebusaway.gtfs.csv.schema.AbstractEntityValidator;
import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs.model.Route;

import java.util.Map;

public class RouteValidator extends AbstractEntityValidator {

  public void validateEntity(CsvEntityContext context, Map<String, Object> csvValues, BeanWrapper object) {
    
    Route route = object.getWrappedInstance(Route.class);

    String shortName = route.getShortName();
    String longName = route.getLongName();

    if ((shortName == null || shortName.length() == 0) && (longName == null || longName.length() == 0))
      throw new IllegalStateException("either shortName or longName must be set for route=" + route.getId());
  }
}
