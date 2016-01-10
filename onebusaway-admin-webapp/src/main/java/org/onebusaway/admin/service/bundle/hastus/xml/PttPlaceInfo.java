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
package org.onebusaway.admin.service.bundle.hastus.xml;

import java.util.ArrayList;
import java.util.List;

public class PttPlaceInfo {
  private String id;
  private String description;
  private String scheduleType;
  private String directionName;
  private List<PttTrip> trips = new ArrayList<PttTrip>();
  private List<PttPlaceInfoPlace> places = new ArrayList<PttPlaceInfoPlace>();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getScheduleType() {
    return scheduleType;
  }

  public void setScheduleType(String scheduleType) {
    this.scheduleType = scheduleType;
  }

  public String getDirectionName() {
    return directionName;
  }

  public void setDirectionName(String directionName) {
    this.directionName = directionName;
  }

  public List<PttTrip> getTrips() {
    return trips;
  }

  public void setTrips(List<PttTrip> trips) {
    this.trips = trips;
  }

  public void addTrip(PttTrip trip) {
    trips.add(trip);
  }

  public List<PttPlaceInfoPlace> getPlaces() {
    return places;
  }

  public void setPlaces(List<PttPlaceInfoPlace> places) {
    this.places = places;
  }

  public void addPlace(PttPlaceInfoPlace place) {
    places.add(place);
  }
}
