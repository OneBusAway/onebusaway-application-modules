/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.washington.cs.rse.transit.common.services;

import edu.washington.cs.rse.transit.common.model.ServicePattern;
import edu.washington.cs.rse.transit.common.model.ServicePatternKey;
import edu.washington.cs.rse.transit.common.model.StopLocation;
import edu.washington.cs.rse.transit.common.model.StopTime;
import edu.washington.cs.rse.transit.common.model.Trip;
import edu.washington.cs.rse.transit.common.model.aggregate.InterpolatedStopTime;
import edu.washington.cs.rse.transit.common.model.aggregate.ScheduledArrivalTime;
import edu.washington.cs.rse.transit.common.model.aggregate.StopTimepointInterpolation;

import java.util.List;
import java.util.Set;

public interface StopSchedulingService {

  public List<ScheduledArrivalTime> getPredictedArrivalsByStopId(int stopId)
      throws NoSuchStopException;

  public List<ScheduledArrivalTime> getArrivalsByServicePatterns(
      Set<ServicePatternKey> ids);

  public List<ScheduledArrivalTime> getArrivalsByServicePatterns(
      Set<ServicePatternKey> ids, long now);

  public List<InterpolatedStopTime> getInterpolatedStopTimes(
      List<StopTimepointInterpolation> stis, List<StopTime> stopTimes);

  public ScheduledArrivalTime getScheduledArrivalTime(ServicePattern pattern,
      Trip trip, StopLocation stop, int stopIndex, long timing);
}