package org.onebusaway.federations;

import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.annotations.FederatedByEntityIdMethod;
import org.onebusaway.federations.annotations.FederatedByEntityIdsMethod;
import org.onebusaway.federations.annotations.FederatedByAggregateMethod;
import org.onebusaway.federations.annotations.FederatedByBoundsMethod;
import org.onebusaway.federations.annotations.FederatedByCoordinateBoundsMethod;
import org.onebusaway.federations.annotations.FederatedByLocationMethod;
import org.onebusaway.geospatial.model.CoordinateBounds;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SimpleFederatedService extends FederatedService {
  
  @FederatedByAggregateMethod
  public List<String> getValuesAsList();
  
  @FederatedByAggregateMethod
  public Map<String,String> getValuesAsMap();
  
  @FederatedByEntityIdMethod
  public String getValueForId(String entityId);
  
  @FederatedByEntityIdMethod(argument=1)
  public String getValueForValueAndId(String value, String entityId);
  
  @FederatedByEntityIdsMethod
  public String getValueForIds(Set<String> entityId);
  
  @FederatedByEntityIdsMethod(argument=1)
  public String getValueForValueAndIds(String value, Set<String> entityId);
  
  @FederatedByBoundsMethod
  public String getValueForBounds(double lat1, double lon1, double lat2, double lon2);
  
  @FederatedByCoordinateBoundsMethod
  public String getValueForCoordinateBounds(CoordinateBounds bounds);
  
  @FederatedByCoordinateBoundsMethod(propertyExpression="bounds")
  public String getValueForCoordinateBoundsTestBean(CoordinateBoundsTestBean bounds);
  
  @FederatedByLocationMethod
  public String getValueForLocation(double lat, double lon);
}
