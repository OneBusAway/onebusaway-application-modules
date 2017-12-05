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
package org.onebusaway.enterprise.webapp.actions.bundles;

public interface ArrivalAndDepartureMessages {

  /****
   * Arrivals and Departures Fields
   ****/
  
  public String arrivedEarly(int minutes);
  
  public String departedEarly(int minutes);

  public String early(int minutes);
  
  public String arrivedOnTime();
  
  public String departedOnTime();

  public String onTime();
  
  public String arrivedLate(int minutes);

  public String departedLate(int minutes);

  public String delayed(int minutes);

  public String scheduledArrival();
  
  public String scheduledDeparture();
}
