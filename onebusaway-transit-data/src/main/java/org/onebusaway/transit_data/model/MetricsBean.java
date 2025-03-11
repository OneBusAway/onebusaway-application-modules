/**
 Copyright (C) 2025 Aaron Brethorst <aaron@onebusaway.org>
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.onebusaway.transit_data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class MetricsBean implements Serializable {
  private static final long serialVersionUID = 1L;

  // Agencies with Coverage

  public int getAgenciesWithCoverageCount() { return agenciesWithCoverageCount; }
  public void setAgenciesWithCoverageCount(int agenciesWithCoverageCount) { this.agenciesWithCoverageCount = agenciesWithCoverageCount; }
  private int agenciesWithCoverageCount;

  public String[] getAgencyIDs() { return agencyIDs; }
  public void setAgencyIDs(String[] agencyIDs) { this.agencyIDs = agencyIDs; }
  private String[] agencyIDs = {};

  // Scheduled Trips Count

  public HashMap<String, Integer> getScheduledTripsCount() { return scheduledTripsCount; }
  public void setScheduledTripsCount(HashMap<String, Integer> scheduledTripsCount) { this.scheduledTripsCount = scheduledTripsCount; }
  private HashMap<String, Integer> scheduledTripsCount;

  // Unmatched Stop IDs
  private HashMap<String, ArrayList<String>> stopIDsUnmatched;
  public HashMap<String, ArrayList<String>> getStopIDsUnmatched() { return stopIDsUnmatched; }
  public void setStopIDsUnmatched(HashMap<String, ArrayList<String>> stopIDsUnmatched) { this.stopIDsUnmatched = stopIDsUnmatched; }

  // Matched Stop IDs Count
  public HashMap<String, Integer> getStopIDsMatchedCount() { return stopIDsMatchedCount; }
  public void setStopIDsMatchedCount(HashMap<String, Integer> stopIDsMatchedCount) { this.stopIDsMatchedCount = stopIDsMatchedCount; }
  private HashMap<String, Integer> stopIDsMatchedCount;
  
}
