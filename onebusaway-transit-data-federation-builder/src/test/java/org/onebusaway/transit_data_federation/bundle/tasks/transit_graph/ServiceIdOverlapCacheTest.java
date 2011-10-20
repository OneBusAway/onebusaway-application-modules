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
package org.onebusaway.transit_data_federation.bundle.tasks.transit_graph;

import static org.junit.Assert.assertEquals;
import static org.onebusaway.collections.CollectionsLibrary.set;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data_federation.bundle.tasks.transit_graph.ServiceIdOverlapCache;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;

public class ServiceIdOverlapCacheTest {

  @Test
  public void test() {

    CalendarService calendarService = Mockito.mock(CalendarService.class);

    TimeZone tz = TimeZone.getDefault();

    AgencyAndId serviceIdA = new AgencyAndId("1", "serviceIdA");
    AgencyAndId serviceIdB = new AgencyAndId("1", "serviceIdB");
    AgencyAndId serviceIdC = new AgencyAndId("1", "serviceIdC");
    AgencyAndId serviceIdD = new AgencyAndId("1", "serviceIdD");

    LocalizedServiceId lsidA = new LocalizedServiceId(serviceIdA, tz);
    LocalizedServiceId lsidB = new LocalizedServiceId(serviceIdB, tz);
    LocalizedServiceId lsidC = new LocalizedServiceId(serviceIdC, tz);
    LocalizedServiceId lsidD = new LocalizedServiceId(serviceIdD, tz);

    Set<ServiceDate> serviceDatesA = set(new ServiceDate(2010, 9, 10),
        new ServiceDate(2010, 9, 11));
    Set<ServiceDate> serviceDatesB = set(new ServiceDate(2010, 9, 11),
        new ServiceDate(2010, 9, 12));
    Set<ServiceDate> serviceDatesC = set(new ServiceDate(2010, 9, 12),
        new ServiceDate(2010, 9, 13));
    Set<ServiceDate> serviceDatesD = set(new ServiceDate(2010, 9, 13));

    Mockito.when(calendarService.getServiceDatesForServiceId(serviceIdA)).thenReturn(
        serviceDatesA);
    Mockito.when(calendarService.getServiceDatesForServiceId(serviceIdB)).thenReturn(
        serviceDatesB);
    Mockito.when(calendarService.getServiceDatesForServiceId(serviceIdC)).thenReturn(
        serviceDatesC);
    Mockito.when(calendarService.getServiceDatesForServiceId(serviceIdD)).thenReturn(
        serviceDatesD);

    ServiceIdOverlapCache cache = new ServiceIdOverlapCache();
    cache.setCalendarService(calendarService);

    List<ServiceIdActivation> combinations = cache.getOverlappingServiceIdCombinations(set(
        lsidA, lsidB, lsidC,lsidD));
    assertEquals(4, combinations.size());
    
    ServiceIdActivation combo = combinations.get(0);
    assertEquals(Arrays.asList(lsidA, lsidB),combo.getActiveServiceIds());
    assertEquals(Arrays.asList(),combo.getInactiveServiceIds());
    
    combo = combinations.get(1);
    assertEquals(Arrays.asList(lsidB, lsidC),combo.getActiveServiceIds());
    assertEquals(Arrays.asList(),combo.getInactiveServiceIds());
    
    combo = combinations.get(2);
    assertEquals(Arrays.asList(lsidC, lsidD),combo.getActiveServiceIds());
    assertEquals(Arrays.asList(),combo.getInactiveServiceIds());
    
    combo = combinations.get(3);
    assertEquals(Arrays.asList(lsidA),combo.getActiveServiceIds());
    assertEquals(Arrays.asList(lsidB),combo.getInactiveServiceIds());

    combinations = cache.getOverlappingServiceIdCombinations(set(lsidA, lsidB,
        lsidC, lsidD));

    Mockito.verify(calendarService, Mockito.times(1)).getServiceDatesForServiceId(
        serviceIdA);
    Mockito.verify(calendarService, Mockito.times(1)).getServiceDatesForServiceId(
        serviceIdB);
    Mockito.verify(calendarService, Mockito.times(1)).getServiceDatesForServiceId(
        serviceIdC);
    Mockito.verify(calendarService, Mockito.times(1)).getServiceDatesForServiceId(
        serviceIdD);
  }
}
