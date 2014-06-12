/**
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;

public class MonitoredResult {

  private List<String> _agencyIds = new ArrayList<String>();
  private Set<String> _unmatchedTripIds = new HashSet<String>();
  private Set<String> _matchedTripIds = new HashSet<String>();
  private Set<String> _unmatchedStopIds = new HashSet<String>();
  private Set<String> _matchedStopIds = new HashSet<String>();
  private Set<AgencyAndId> _unmatchedBlockIds = new HashSet<AgencyAndId>();
  private Set<CoordinatePoint> _allCoordinates = new HashSet<CoordinatePoint>();
  private int _recordsTotal = 0;
  private long _lastUpdate = 0; // we want the default to be very very old, to catch stale updates
  
  public void addUnmatchedTripId(String tripId) {
    _unmatchedTripIds.add(tripId);
  }

  public Set<String> getUnmatchedTripIds() {
    return _unmatchedTripIds;
  }

  public void addUnmatchedStopId(String stopId) {
	  _unmatchedStopIds.add(stopId);
  }
  
  public Set<String> getUnmatchedStopIds() {
	  return _unmatchedStopIds;
  }
  
  public void addMatchedStopId(String stopId) {
	  _matchedStopIds.add(stopId);
  }
  
  public Set<String> getMatchedStopIds() {
	  return _matchedStopIds;
  }
  
  public void addUnmatchedBlockId(AgencyAndId id) {
    _unmatchedBlockIds.add(id);
  }

  public Set<AgencyAndId> getUnmatchedBlockIds() {
    return _unmatchedBlockIds;
  }
  
  void setRecordsTotal(int size) {
    _recordsTotal = size;
  }
  
  public int getRecordsTotal() {
    return _recordsTotal;
  }

  public void addRecordTotal() {
    _recordsTotal = _recordsTotal + 1;
    
  }

  public void addAgencyId(String agencyId) {
    _agencyIds.add(agencyId);
  }
  
  public List<String> getAgencyIds() {
    return _agencyIds;
  }

  public void setAgencyIds(List<String> agencyIds) {
    _agencyIds = agencyIds;
  }
  
  public long getLastUpdate() {
    return _lastUpdate;
  }

  public void setLastUpdate(long updateTimeInMillis) {
    _lastUpdate = updateTimeInMillis;
  }
  
  public Set<String> getMatchedTripIds() {
    return _matchedTripIds;
  }

  public void setMatchedTripIds(Set<String> allTripIds) {
    this._matchedTripIds = allTripIds;
  }

  public void addMatchedTripId(String tripId) {
    this._matchedTripIds.add(tripId);
  }

  public Set<CoordinatePoint> getAllCoordinates() {
    return _allCoordinates;
  }

  public void addLatLon(float latitude, float longitude) {
    CoordinatePoint cp = new CoordinatePoint(latitude, longitude);
    _allCoordinates.add(cp);
  }

  public void addLatLon(double latitude, double longitude) {
    CoordinatePoint cp = new CoordinatePoint(latitude, longitude);
    _allCoordinates.add(cp);
  }
}
