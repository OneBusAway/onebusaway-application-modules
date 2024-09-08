/**
 * Copyright (C) 2024 Angelo Cassano
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

package org.onebusaway.transit_data_federation.impl.beans;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.exceptions.NoSuchAgencyServiceException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data_federation.impl.transit_graph.AgencyEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.util.AgencyAndIdLibrary;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class StopsBeanServiceImplTest {
    private StopsBeanServiceImpl _service;
    private StopBeanService _stopBeanService;
    private TransitGraphDao _transitGraphDao;

    @Before
    public void setup() {
        _service = new StopsBeanServiceImpl();

        _stopBeanService = Mockito.mock(StopBeanService.class);
        _service.setStopBeanService(_stopBeanService);

        _transitGraphDao = Mockito.mock(TransitGraphDao.class);
        _service.setTransitGraphDao(_transitGraphDao);
    }

    @Test
    public void testGetStopsIdsForAgencyId() {
        List<StopEntry> stopEntries = new ArrayList<StopEntry>();
        List<String> stopIds = new ArrayList<String>();

        String agencyId = "1";
        AgencyEntryImpl agency = new AgencyEntryImpl();
        agency.setStops(stopEntries);

        Mockito.when(_transitGraphDao.getAgencyForId(agencyId)).thenReturn(agency);
        for (int i = 0; i < 10; i++) {
            AgencyAndId id = new AgencyAndId(agencyId, Integer.toString(i));
            StopEntryImpl stopEntry = new StopEntryImpl(id, 10 * (i + 1), -20 * (i + 1));
            stopEntries.add(stopEntry);
            stopIds.add(AgencyAndIdLibrary.convertToString(id));
        }

        ListBean<String> stopIdsFromService = _service.getStopsIdsForAgencyId(agencyId);

        assertNotNull(stopIdsFromService);
        assertNotNull(stopIdsFromService.getList());
        assertEquals(10, stopIdsFromService.getList().size());

        for (int i = 0; i < 10; i++) {
            assertEquals(stopIds.get(i), stopIdsFromService.getList().get(i));
        }
    }

    @Test
    public void testGetStopsIdsForAgencyIdThrowsNoSuchAgencyServiceException() {
        String agencyId = "1";
        Mockito.when(_transitGraphDao.getAgencyForId(agencyId)).thenReturn(null);

        try {
            _service.getStopsIdsForAgencyId(agencyId);
            fail("Should throw NoSuchAgencyServiceException");
        } catch (Throwable e) {
            assertTrue(e instanceof NoSuchAgencyServiceException);
        }
    }

    @Test
    public void testGetStopsForAgencyId() {
        List<StopEntry> stopEntries = new ArrayList<StopEntry>();
        List<StopBean> stopBeans = new ArrayList<StopBean>();

        String agencyId = "1";
        AgencyEntryImpl agency = new AgencyEntryImpl();
        agency.setStops(stopEntries);

        Mockito.when(_transitGraphDao.getAgencyForId(agencyId)).thenReturn(agency);
        for (int i = 0; i < 10; i++) {
            AgencyAndId id = new AgencyAndId(agencyId, Integer.toString(i));
            StopEntryImpl stopEntry = new StopEntryImpl(id, 10 * (i + 1), -20 * (i + 1));
            StopBean stopBean = new StopBean();

            stopBean.setId(id.getId());
            stopBean.setLat(stopEntry.getStopLat());
            stopBean.setLon(stopEntry.getStopLon());

            stopEntries.add(stopEntry);
            stopBeans.add(stopBean);

            Mockito.when(_transitGraphDao.getStopEntryForId(id)).thenReturn(stopEntry);
            Mockito.when(_stopBeanService.getStopForId(id, null)).thenReturn(stopBean);
        }

        StopsBean stopsBean = _service.getStopsForAgencyId(agencyId);

        assertNotNull(stopsBean);
        assertNotNull(stopsBean.getStops());
        assertEquals(10, stopsBean.getStops().size());

        for (int i = 0; i < 10; i++) {
            StopBean stopBean = stopBeans.get(i);
            StopBean stopBeanFromService = stopsBean.getStops().get(i);

            assertNotNull(stopBeanFromService);
            assertEquals(stopBean.getId(), stopBeanFromService.getId());
            assertEquals(stopBean.getLat(), stopBeanFromService.getLat(), 0.0);
            assertEquals(stopBean.getLon(), stopBeanFromService.getLon(), 0.0);
        }
    }

    @Test
    public void testGetStopsForAgencyIdThrowsNoSuchAgencyServiceException() {
        String agencyId = "1";
        Mockito.when(_transitGraphDao.getAgencyForId(agencyId)).thenReturn(null);

        try {
            _service.getStopsForAgencyId(agencyId);
            fail("Should throw NoSuchAgencyServiceException");
        } catch (Throwable e) {
            assertTrue(e instanceof NoSuchAgencyServiceException);
        }
    }
}

