/**
 * Copyright (C) 2026 OneBusAway
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
package org.onebusaway.api.model.where;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Regression coverage for a copy-paste bug where hasPredictedDepartureTime()
 * checked predictedArrivalTime instead of predictedDepartureTime, which made
 * computeBestDepartureTime() ignore a real predicted departure whenever the
 * predicted arrival was absent (and vice versa, falsely report a predicted
 * departure that was never set).
 */
public class ArrivalAndDepartureBeanV1Test {

  @Test
  public void hasPredictedDepartureTimeAndComputeBestDepartureTimeWithNoPredictedArrival() {
    ArrivalAndDepartureBeanV1 bean = new ArrivalAndDepartureBeanV1();
    bean.setScheduledArrivalTime(1000L);
    bean.setScheduledDepartureTime(2000L);
    bean.setPredictedArrivalTime(0L);
    bean.setPredictedDepartureTime(2500L);

    assertTrue(bean.hasPredictedDepartureTime());
    assertEquals(2500L, bean.computeBestDepartureTime());
  }

  @Test
  public void hasPredictedDepartureTimeAndComputeBestDepartureTimeWithNoPredictedDeparture() {
    ArrivalAndDepartureBeanV1 bean = new ArrivalAndDepartureBeanV1();
    bean.setScheduledArrivalTime(1000L);
    bean.setScheduledDepartureTime(2000L);
    bean.setPredictedArrivalTime(1500L);
    bean.setPredictedDepartureTime(0L);

    assertFalse(bean.hasPredictedDepartureTime());
    assertEquals(2000L, bean.computeBestDepartureTime());
  }

}
