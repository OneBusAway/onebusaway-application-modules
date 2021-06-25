/**
 * Copyright (C) 2019 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.assignments.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onebusaway.admin.model.assignments.ActiveBlock;
import org.onebusaway.admin.model.assignments.TripSummary;
import org.onebusaway.admin.service.assignments.AssignmentConfigDao;
import org.onebusaway.admin.service.assignments.AssignmentConfigService;
import org.onebusaway.admin.service.assignments.AssignmentDao;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.blocks.*;
import org.onebusaway.transit_data.model.schedule.StopTimeBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.impl.federated.TransitDataServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

@ContextConfiguration(locations = "classpath:org/onebusaway/admin/application-context-test.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class VehicleAssignmentServiceImplTest {

    @Autowired
    private AssignmentConfigService _assignmentConfigService;

    @Autowired
    private AssignmentDao _assignmentDao;

    private static String TRIP_ID = "TripId";
    private static String HEADSIGN = "HeadSign";
    private static String START_TIME ="00:00:00";
    private static String END_TIME = "02:46:40";
    private static String AGENCY_ID ="1";
    private static String ROUTE_ID ="Route";
    private static String VEHICLE_ID ="Vehicle_1";
    private static String BLOCK_ID ="1";

    @Before
    public void setup(){
        _assignmentConfigService.deleteAll();
        _assignmentDao.deleteAll();
    }

    private TransitDataService tds = new TransitDataServiceImpl() {

        public ListBean<RouteBean> getRoutesForAgencyId(String agencyId) {
            ListBean<RouteBean> lb = new ListBean<>();
            lb.setList(new ArrayList<RouteBean>());
            RouteBean.Builder builder = RouteBean.builder();
            builder.setId("1_route1");
            AgencyBean agency = new AgencyBean();
            agency.setTimezone("America/New_York");
            builder.setAgency(agency);

            RouteBean rb = builder.create();
            lb.getList().add(rb);
            return lb;
        }


        public ListBean<TripDetailsBean> getTripsForRoute(TripsForRouteQueryBean query) {
            ListBean<TripDetailsBean> lb = new ListBean<>();
            lb.setList(new ArrayList<TripDetailsBean>());
            TripDetailsBean tdb = new TripDetailsBean();
            tdb.setTrip(new TripBean());
            tdb.getTrip().setBlockId("1");
            tdb.getTrip().setServiceId("1_service1");
            lb.getList().add(tdb);
            return lb;
        }

        public BlockBean getBlockForId(String blockId) {
            List<BlockConfigurationBean> blockConfigurations = new ArrayList<>();
            blockConfigurations.add(getBlockConfiguration());

            BlockBean bb = new BlockBean();
            bb.setId(blockId);
            bb.setConfigurations(blockConfigurations);
            return bb;
        }

        public BlockInstanceBean getBlockInstance(String blockId, long serviceDate) {
            BlockInstanceBean bib = new BlockInstanceBean();
            bib.setBlockId(blockId);
            bib.setBlockConfiguration(getBlockConfiguration());
            return bib;
        }

        @Override
        public RouteBean getRouteForId(String routeId) throws ServiceException {
            RouteBean.Builder builder = RouteBean.builder();
            builder.setId("1_route1");
            AgencyBean agency = new AgencyBean();
            agency.setTimezone("America/New_York");
            builder.setAgency(agency);
            return builder.create();
        }

        public List<BlockInstanceBean> getActiveBlocksForRoute(AgencyAndId agencyAndRouteId, long fromTime, long toTime){

            List<BlockInstanceBean> blockInstanceBeanList = new ArrayList<>();
            BlockInstanceBean blockInstance = getBlockInstance("1", fromTime);

            blockInstanceBeanList.add(blockInstance);
            return blockInstanceBeanList;
        }

        private BlockConfigurationBean getBlockConfiguration(){
            StopTimeBean stopTimeStart = new StopTimeBean();
            stopTimeStart.setArrivalTime(0);

            BlockStopTimeBean blockStopTimeBeanStart = new BlockStopTimeBean();
            blockStopTimeBeanStart.setStopTime(stopTimeStart);

            StopTimeBean stopTimeEnd = new StopTimeBean();
            stopTimeEnd.setArrivalTime(10000);

            BlockStopTimeBean blockStopTimeBeanEnd = new BlockStopTimeBean();
            blockStopTimeBeanEnd.setStopTime(stopTimeEnd);

            List<BlockStopTimeBean> blockStopTimeBeans = new ArrayList<>();
            blockStopTimeBeans.add(blockStopTimeBeanStart);
            blockStopTimeBeans.add(blockStopTimeBeanEnd);

            TripBean tripBean = new TripBean();
            tripBean.setId(TRIP_ID);
            tripBean.setTripHeadsign(HEADSIGN);

            BlockTripBean blockTripBean = new BlockTripBean();
            blockTripBean.setTrip(tripBean);
            blockTripBean.setBlockStopTimes(blockStopTimeBeans);

            List<BlockTripBean> blockTripBeanList = new ArrayList<>();
            blockTripBeanList.add(blockTripBean);


            BlockConfigurationBean blockConfigurationBean = new BlockConfigurationBean();
            blockConfigurationBean.setTrips(blockTripBeanList);

            return blockConfigurationBean;
        }
    };

    @Test
    public void activeBlocksTest() throws ExecutionException {
        VehicleAssignmentServiceImpl vas = new VehicleAssignmentServiceImpl();
        vas.setAssignmentDao(_assignmentDao);
        vas.setAssignmentConfigService(_assignmentConfigService);
        vas.setTransitDataService(tds);

        AgencyAndId route = new AgencyAndId(AGENCY_ID,ROUTE_ID);

        List<AgencyAndId> routeList = new ArrayList<>();
        routeList.add(route);

        ActiveBlock activeBlock = new ActiveBlock();
        activeBlock.setBlockId("1");

        List<ActiveBlock> retreivedActiveBlocks = vas.getActiveBlocks(new ServiceDate(), routeList);
        ActiveBlock retreivedActiveBlock = retreivedActiveBlocks.get(0);


        assertEquals(retreivedActiveBlock.getBlockId(), activeBlock.getBlockId());

    }

    @Test
    public void assignmentByBlockIdTest() throws ExecutionException {
        VehicleAssignmentServiceImpl vas = new VehicleAssignmentServiceImpl();
        vas.setAssignmentDao(_assignmentDao);
        vas.setAssignmentConfigService(_assignmentConfigService);
        vas.setTransitDataService(tds);

        String vehicleId = vas.getAssignmentByBlockId("1");

        assertNull(vehicleId);

        vas.assign(BLOCK_ID, VEHICLE_ID);

        vehicleId = vas.getAssignmentByBlockId("1");

        assertEquals(VEHICLE_ID, vehicleId);

    }

    @Test
    public void lastUpdatedTest() {

        VehicleAssignmentServiceImpl vas = new VehicleAssignmentServiceImpl();
        vas.setAssignmentDao(_assignmentDao);
        vas.setAssignmentConfigService(_assignmentConfigService);
        vas.setTransitDataService(tds);

        Date lastUpdated = vas.getLastUpdated();

        assertNull(lastUpdated);

        vas.assign(BLOCK_ID, VEHICLE_ID);

        lastUpdated = vas.getLastUpdated();

        assertNotNull(lastUpdated);

    }

    @Test
    public void tripsForBlockTest() throws ExecutionException {

        VehicleAssignmentServiceImpl vas = new VehicleAssignmentServiceImpl();
        vas.setAssignmentDao(_assignmentDao);
        vas.setAssignmentConfigService(_assignmentConfigService);
        vas.setTransitDataService(tds);
        vas.setup();

        TripSummary retrievedTripSummary = vas.getTripsForBlock(BLOCK_ID).get(0);

        assertEquals(TRIP_ID,retrievedTripSummary.getTripId());
        assertEquals(HEADSIGN,retrievedTripSummary.getHeadSign());
        assertEquals(START_TIME,retrievedTripSummary.getStartTime());
        assertEquals(END_TIME,retrievedTripSummary.getEndTime());

    }

    @Test
    public void resetAssignmentsTest(){
        VehicleAssignmentServiceImpl vas = new VehicleAssignmentServiceImpl();
        vas.setAssignmentDao(_assignmentDao);
        vas.setAssignmentConfigService(_assignmentConfigService);
        vas.setTransitDataService(tds);
        vas.setup();

        vas.assign(BLOCK_ID, VEHICLE_ID);

        assertEquals(1, vas.getAssignments().size());
        assertNotNull(vas.getLastUpdated());

        vas.resetAssignments();

        assertEquals(0, vas.getAssignments().size());
        assertNull(vas.getLastUpdated());

    }

    @Test
    public void testDateTransition() throws ParseException {
        VehicleAssignmentServiceImpl vas = new VehicleAssignmentServiceImpl();
        VehicleAssignmentServiceImpl vasSpy = Mockito.spy(vas);
        vasSpy.setAssignmentDao(_assignmentDao);
        vasSpy.setAssignmentConfigService(_assignmentConfigService);
        vasSpy.setTransitDataService(tds);

        SimpleDateFormat sdf =new SimpleDateFormat("dd/MM/yyyy");
        Date firstDate = sdf.parse("01/01/2019");
        Date secondDate = sdf.parse("01/02/2019");

        Mockito.when(vasSpy.getCurrentDate()).thenReturn(firstDate);

        vasSpy.assign(BLOCK_ID, VEHICLE_ID);

        assertEquals(1, vasSpy.getAssignments().size());

        Mockito.when(vasSpy.getCurrentDate()).thenReturn(secondDate);

        assertEquals(0, vasSpy.getAssignments().size());


    }

    private static Date getDate(Date serviceDate) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(serviceDate);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

}
