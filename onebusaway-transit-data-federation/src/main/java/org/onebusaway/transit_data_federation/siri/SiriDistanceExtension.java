/**
 * Copyright (C) 2010 OpenPlans
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
package org.onebusaway.transit_data_federation.siri;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A special addition to the XSD-generated SIRI classes to encapsulate
 * the MTA-specific distance-based formulations of arrivals.
 * 
 * These have been submitted as extensions to the official SIRI spec. 
 * 
 * @author jmaki
 *
 */
@XmlRootElement
public class SiriDistanceExtension {

  private Integer StopsFromCall = null;

  private Double CallDistanceAlongRoute = null;    

  private Double DistanceFromCall = null;

  private String PresentableDistance = null;
  
  @XmlElement(name="StopsFromCall")
  public Integer getStopsFromCall() {
    return StopsFromCall;
  }

  public void setStopsFromCall(Integer stopsFromCall) {
    StopsFromCall = stopsFromCall;
  }

  @XmlElement(name="CallDistanceAlongRoute")
  public Double getCallDistanceAlongRoute() {
    return CallDistanceAlongRoute;
  }

  public void setCallDistanceAlongRoute(Double callDistanceAlongRoute) {
    CallDistanceAlongRoute = callDistanceAlongRoute;
  }

  @XmlElement(name="DistanceFromCall")
  public Double getDistanceFromCall() {
    return DistanceFromCall;
  }

  public void setDistanceFromCall(Double distanceFromCall) {
    DistanceFromCall = distanceFromCall;
  }

  @XmlElement(name="PresentableDistance")
  public String getPresentableDistance() {
    return PresentableDistance;
  }

  public void setPresentableDistance(String presentableDistance) {
    PresentableDistance = presentableDistance;
  }

}