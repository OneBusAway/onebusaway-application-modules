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

import java.util.List;
import java.util.Set;

import edu.washington.cs.rse.transit.common.impl.TimingBean;
import edu.washington.cs.rse.transit.common.model.ServicePatternKey;
import edu.washington.cs.rse.transit.common.model.aggregate.ScheduledArrivalTime;

public interface TimepointSchedulingService {

    public int getPreWindow();

    public int getPostWindow();

    public List<ScheduledArrivalTime> getPredictedArrivalsByTimepointId(int timepointId) throws NoSuchStopException;

    public List<ScheduledArrivalTime> getPredictedArrivalsByTimepointId(int timepointId, long time)
            throws NoSuchStopException;

    public List<ScheduledArrivalTime> getPredictedArrivalsByTimepointIds(Set<Integer> ids);

    public List<ScheduledArrivalTime> getPredictedArrivalsByTimepointIds(Set<Integer> ids, long time);

    public List<ScheduledArrivalTime> getPredictedArrivalsByServicePatterns(Set<ServicePatternKey> ids,
            TimingBean timing);
}