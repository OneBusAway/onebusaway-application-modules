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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.onebusaway.admin.model.assignments.ActiveBlock;
import org.onebusaway.admin.model.assignments.Assignment;
import org.onebusaway.admin.model.assignments.AssignmentDate;
import org.onebusaway.admin.model.assignments.TripSummary;
import org.onebusaway.admin.service.assignments.ActiveVehiclesService;
import org.onebusaway.admin.service.assignments.AssignmentDao;
import org.onebusaway.admin.service.assignments.AssignmentDateDao;
import org.onebusaway.admin.service.assignments.VehicleAssignmentService;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.blocks.*;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
public class VehicleAssignmentServiceImpl implements VehicleAssignmentService {

    private static Logger _log = LoggerFactory.getLogger(VehicleAssignmentServiceImpl.class);

    @Autowired
    private TransitDataService _tds;
    public void setTransitDataService(TransitDataService tds) {
        _tds = tds;
    }

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private AssignmentDao assignmentDao;

    @Autowired
    private AssignmentDateDao assignmentDateDao;

    @Autowired
    private ActiveVehiclesService activeVehicleService;

    LoadingCache<ServiceDate, List<ActiveBlock>> activeBlocksCache;

    LoadingCache<String, List<TripSummary>> tripSummaryCache;


    public void setAssignmentDao(AssignmentDao assignmentDao) {
        this.assignmentDao = assignmentDao;
    }

    public void setAssignmentDateDao(AssignmentDateDao assignmentDateDao) {
        this.assignmentDateDao = assignmentDateDao;
    }

    @PostConstruct
    public void setup(){
        CacheLoader<ServiceDate,  List<ActiveBlock>> activeBlocksLoader;
        activeBlocksLoader = new CacheLoader<ServiceDate, List<ActiveBlock>>() {
            @Override
            public List<ActiveBlock> load(ServiceDate serviceDate) {
                return getUncachedActiveBlocks(serviceDate);
            }
        };

        activeBlocksCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1,TimeUnit.MINUTES)
                .build(activeBlocksLoader);


        CacheLoader<String,  List<TripSummary>> tripSummaryLoader;
        tripSummaryLoader = new CacheLoader<String, List<TripSummary>>() {
            @Override
            public List<TripSummary> load(String blockId) {
                return getUncachedTripsForBlock(blockId);
            }
        };

        tripSummaryCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1,TimeUnit.MINUTES)
                .build(tripSummaryLoader);
    }

    @Override
    public Date getLastUpdated(){
        AssignmentDate assignmentDate = assignmentDateDao.getAssignmentDate("lastUpdated");
        if(assignmentDate != null){
            if(assignmentDate.getValue() != null){
                return assignmentDate.getValue();
            }
        }
        return null;
    }

    @Override
    public boolean assign(String blockId, String vehicleId) {
        return insert(blockId, vehicleId);
    }

    @Override
    public String getAssignmentByBlockId(String blockId) {
        Assignment assignment = assignmentDao.getAssignment(blockId);
        if(assignment != null){
            return assignment.getVehicleId();
        }
        return null;
    }

    @Override
    public Map<String, String> getAssignments() {
        Map<String, String> sortedMap = new TreeMap<String, String>();
        List<Assignment> assignments = assignmentDao.getAll();
        for(Assignment assignment :  assignments){
            sortedMap.put(assignment.getBlockId(), assignment.getVehicleId());
        }
        return sortedMap;
    }

    @Override
    public List<String> getActiveVehicles(String query){
        return activeVehicleService.getActiveVehicles(query);
    }

    @Override
    public List<String> getActiveVehicles(){
        return activeVehicleService.getActiveVehicles();
    }

    @Override
    public List<ActiveBlock> getActiveBlocks(ServiceDate serviceDate) throws ExecutionException {
        return activeBlocksCache.get(serviceDate);
    }

    @Override
    public List<ActiveBlock> getActiveBlocks(ServiceDate serviceDate, List<AgencyAndId> filterRoutes) throws ExecutionException {
        return getUncachedActiveBlocks(serviceDate, filterRoutes);
    }

    private List<ActiveBlock> getUncachedActiveBlocks(ServiceDate serviceDate){
        List<AgencyAndId> filterRoutes = getVehicleAssignmentRoutes();
        return getUncachedActiveBlocks(serviceDate, filterRoutes);
    }

    private List<ActiveBlock> getUncachedActiveBlocks(ServiceDate serviceDate, List<AgencyAndId> filterRoutes){
        Map<String, ActiveBlock> activeBlocks = new HashMap<>();

        for (AgencyAndId agencyAndRouteId : filterRoutes) {
            _log.debug("agency and route id=" + agencyAndRouteId);
            RouteBean rb = _tds.getRouteForId(AgencyAndId.convertToString(agencyAndRouteId));
            _log.debug("route for agency = " + rb.getId());
            if(rb !=null) {
                TimeZone tz = TimeZone.getTimeZone(rb.getAgency().getTimezone());
                Date date = serviceDate.getAsDate(tz);
                long fromTime = getFirstTimeOfDate(date);
                long toTime = getLastTimeOfDay(date);

                List<BlockInstanceBean> blockInstanceBeans = _tds.getActiveBlocksForRoute(agencyAndRouteId, fromTime, toTime);

                for (BlockInstanceBean blockInstanceBean : blockInstanceBeans) {
                    if (!activeBlocks.containsKey(blockInstanceBean.getBlockId())) {
                        activeBlocks.put(blockInstanceBean.getBlockId(), getActiveBlock(blockInstanceBean, agencyAndRouteId.getId()));
                    } else {
                        activeBlocks.get(blockInstanceBean.getBlockId()).getRoutes().add(agencyAndRouteId.getId());
                    }
                }
            }
        }
        return new ArrayList<>(activeBlocks.values());
    }

    @Override
    public List<TripSummary> getTripsForBlock(String blockId) throws ExecutionException {
        return tripSummaryCache.get(blockId);
    }

    private List<TripSummary> getUncachedTripsForBlock(String blockId){
        BlockBean blockBean = _tds.getBlockForId(blockId);
        Set<TripSummary> tripSummaries = new LinkedHashSet<>();
        for(BlockConfigurationBean blockConfigurationn : blockBean.getConfigurations()){
            for(BlockTripBean blockTrip : blockConfigurationn.getTrips()){
                TripBean trip = blockTrip.getTrip();
                List<BlockStopTimeBean> blockStopTimes = blockTrip.getBlockStopTimes();

                TripSummary tripSummary = new TripSummary();
                tripSummary.setTripId(trip.getId());
                tripSummary.setHeadSign(trip.getTripHeadsign());
                if(blockStopTimes != null && blockStopTimes.size() > 0){
                    int startTime = blockStopTimes.get(0).getStopTime().getArrivalTime();
                    int endTime = blockStopTimes.get(blockStopTimes.size() -1).getStopTime().getArrivalTime();
                    tripSummary.setStartTime(convertSecondsToTime(startTime));
                    tripSummary.setEndTime(convertSecondsToTime(endTime));
                }
                tripSummaries.add(tripSummary);
            }
        }
        return new ArrayList<>(tripSummaries);
    }

    private List<AgencyAndId> getVehicleAssignmentRoutes(){
        List<AgencyAndId> routesAsAgencyAndId = new ArrayList<>();
        String vehicleAssignmentRoutes = configurationService.getConfigurationValueAsString("vehicleAssignmentRoutes", "");
        List<String> routes = Arrays.asList(vehicleAssignmentRoutes.split("\\s*,\\s*"));
        for(String route : routes){
            routesAsAgencyAndId.add(AgencyAndId.convertFromString(route));
        }
        return routesAsAgencyAndId;
    }

    private static long getFirstTimeOfDate(Date serviceDate) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(serviceDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 000);
        return cal.getTime().getTime();
    }

    private static long getLastTimeOfDay(Date serviceDate) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(serviceDate);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime().getTime();
    }

    private ActiveBlock getActiveBlock(BlockInstanceBean blockInstance, String routeId){
        ActiveBlock activeBlock = new ActiveBlock();
        activeBlock.setBlockId(blockInstance.getBlockId());
        activeBlock.setBlockInstanceBean(blockInstance);
        activeBlock.setStartTime(getBlockStartTime(blockInstance));
        activeBlock.setEndTime(getBlockEndTime(blockInstance));
        activeBlock.getRoutes().add(routeId);
        return activeBlock;
    }

    private String getBlockStartTime(BlockInstanceBean blockInstanceBean){
        List<BlockTripBean> trips = blockInstanceBean.getBlockConfiguration().getTrips();
        if(trips != null && trips.size() > 0){
            List<BlockStopTimeBean> blockStopTimes = trips.get(0).getBlockStopTimes();
            if(blockInstanceBean != null && trips.size() > 0){
                int arrivalTime =  blockStopTimes.get(0).getStopTime().getArrivalTime();
                return convertSecondsToTime(arrivalTime);
            }
        }
        return "xx:xx:xx";
    }

    private String getBlockEndTime(BlockInstanceBean blockInstanceBean){
        List<BlockTripBean> trips = blockInstanceBean.getBlockConfiguration().getTrips();
        if(trips != null && trips.size() > 0){
            List<BlockStopTimeBean> blockStopTimes = trips.get(trips.size() -1).getBlockStopTimes();
            if(blockInstanceBean != null && trips.size() > 0){
                int arrivalTime =  blockStopTimes.get(blockStopTimes.size() -1).getStopTime().getArrivalTime();
                return convertSecondsToTime(arrivalTime);
            }
        }
        return "xx:xx:xx";
    }

    private static String convertSecondsToTime(int arrivalTime){
        long hours = TimeUnit.SECONDS.toHours(arrivalTime);
        long minutes = TimeUnit.SECONDS.toMinutes(arrivalTime) - (TimeUnit.SECONDS.toHours(arrivalTime)* 60);
        long seconds = TimeUnit.SECONDS.toSeconds(arrivalTime) - (TimeUnit.SECONDS.toMinutes(arrivalTime) *60);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private boolean insert(String blockId, String vehicleId) {
        Assignment assignment = new Assignment(blockId, vehicleId);
        assignmentDao.save(assignment);

        AssignmentDate assignmentDate = new AssignmentDate("lastUpdated", new Date());
        assignmentDateDao.save(assignmentDate);

        return true;
    }
}
