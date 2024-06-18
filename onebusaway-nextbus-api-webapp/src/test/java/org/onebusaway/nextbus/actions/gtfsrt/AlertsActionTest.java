/**
 * Copyright (C) 2024 Cambridge Systematics
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
package org.onebusaway.nextbus.actions.gtfsrt;

import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.nextbus.impl.gtfsrt.GtfsrtCache;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Test matching regex on AlertsAction.
 */
public class AlertsActionTest {

  @Test
  public void matchesFilter() {
    final String source = "WMATA_alert|console";
    GtfsrtCache cache = Mockito.mock(GtfsrtCache.class);
    AlertsAction action = new AlertsAction();
    action.setCache(cache);
    when(cache.getAlertFilter()).thenReturn(source);

    // test 0:  no source set
    ServiceAlertBean bean = new ServiceAlertBean();
    bean.setSource(null);
    assertTrue(action.matchesFilter(bean));
    // test I:  WMATA_alert
    bean.setSource("WMATA_alert");
    assertTrue(action.matchesFilter(bean));
    // test II: console
    bean.setSource("console");
    assertTrue(action.matchesFilter(bean));
    // negative test III:  WMATA_advisory
    bean.setSource("WMATA_advisory");
    assertFalse(action.matchesFilter(bean));

  }
}