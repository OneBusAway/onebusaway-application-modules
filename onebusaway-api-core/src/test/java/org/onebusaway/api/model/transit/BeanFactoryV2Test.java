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
package org.onebusaway.api.model.transit;

import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;

public class BeanFactoryV2Test {

  /**
   * A {@link StopBean} can carry a null {@link RouteBean} in its routes list
   * when the bundle's stop->route-collection index references a collection that
   * no longer resolves (the production trigger for issue #461). The serializer
   * must skip it rather than NPE, which would otherwise unwind out of the action
   * and emit a bare "null" response body.
   */
  @Test
  public void getStopSkipsNullRoute() {
    BeanFactoryV2 factory = new BeanFactoryV2(true);

    StopBean stop = new StopBean();
    stop.setId("1_stop");
    stop.setRoutes(Collections.singletonList((RouteBean) null));

    StopV2Bean bean = factory.getStop(stop);

    assertTrue(bean.getRouteIds().isEmpty());
  }
}
