/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
  
  @FederatedByEntityIdMethod(propertyExpression="id")
  public String getValueForValueBean(EntityIdTestBean value);
  
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
