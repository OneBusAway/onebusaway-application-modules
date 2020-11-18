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
import org.onebusaway.admin.model.assignments.TripSummary;
import org.onebusaway.admin.service.assignments.*;
import org.onebusaway.admin.util.DateTimeUtil;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.onebusaway.admin.util.DateTimeUtil.*;

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

    private ScheduledExecutorService _executor;

    @Autowired
    private AssignmentDao assignmentDao;

    @Autowired
    private AssignmentConfigService assignmentConfigService;

    @Autowired
    private ActiveVehiclesService activeVehicleService;

    LoadingCache<Date, List<ActiveBlock>> activeBlocksCache;

    LoadingCache<String, List<TripSummary>> tripSummaryCache;

    public void setAssignmentDao(AssignmentDao assignmentDao) {
        this.assignmentDao = assignmentDao;
    }

    public void setAssignmentConfigService(AssignmentConfigService assignmentConfigService) {
        this.assignmentConfigService = assignmentConfigService;
    }

    @PostConstruct
    public void setup(){
        _executor = Executors.newSingleThreadScheduledExecutor();
        // check for bundle change every 60 seconds
        _executor.scheduleAtFixedRate(new VehicleAssignmentServiceImpl.CheckForBundleChange(), 0, 60, TimeUnit.SECONDS);

        CacheLoader<Date,  List<ActiveBlock>> activeBlocksLoader;
        activeBlocksLoader = new CacheLoader<Date, List<ActiveBlock>>() {
            @Override
            public List<ActiveBlock> load(Date serviceDate) {
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
        Date lastUpdated = assignmentConfigService.getConfigValueAsDateTime("lastUpdated");
        if(lastUpdated != null){
            return lastUpdated;
        }
        return null;
    }

    @Override
    public boolean assign(String blockId, String vehicleId, Date date) {
        Date serviceDate = getStartOfServiceDay(date);
        Assignment assignment = new Assignment(blockId, vehicleId, serviceDate);
        assignmentDao.save(assignment);

        assignmentConfigService.setConfigValueAsDateTime("lastUpdated", getCurrentDate());

        return true;
    }

    @Override
    public boolean assign(String blockId, String vehicleId) {
        Date currentDate = getCurrentDate();
        return assign(blockId, vehicleId, currentDate);
    }


    @Override
    public List<Assignment> getAssignments(){
        return getAssignments(getCurrentDate());
    }

    @Override
    public List<Assignment> getAssignments(Date date){
        List<Assignment> assignments = assignmentDao.getAll(date);
        return assignments;
    }

    @Override
    public Map<String, String> getAssignmentsAsMap() {
        Map<String, String> sortedMap = new LinkedHashMap<String, String>();
        for(Assignment assignment :  getAssignments()){
            sortedMap.put(assignment.getBlockId(), assignment.getVehicleId());
        }
        return sortedMap;
    }

    @Override
    public Map<String, String> getAssignmentsAsMap(Date date) {
        Map<String, String> sortedMap = new LinkedHashMap<String, String>();
        for(Assignment assignment :  getAssignments(date)){
            sortedMap.put(assignment.getBlockId(), assignment.getVehicleId());
        }
        return sortedMap;
    }

    @Override
    public String getAssignmentByBlockId(String blockId, Date date) {
        Assignment assignment = assignmentDao.getAssignment(blockId, date);
        if(assignment != null){
            return assignment.getVehicleId();
        }
        return null;
    }

    @Override
    public String getAssignmentByBlockId(String blockId) {
        Assignment assignment = assignmentDao.getAssignment(blockId, getCurrentDate());
        if(assignment != null){
            return assignment.getVehicleId();
        }
        return null;
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
        Date updatedServiceDate = getAdjustedServiceDate(serviceDate);

        if (activeBlocksCache != null) {
            // on startup we may be called before initialized
            return activeBlocksCache.get(updatedServiceDate);
        }
        return new ArrayList<>();
    }

    @Override
    public List<ActiveBlock> getActiveBlocks(ServiceDate serviceDate, List<AgencyAndId> filterRoutes) throws ExecutionException {
        Date updatedServiceDate = getAdjustedServiceDate(serviceDate);
        return getUncachedActiveBlocks(updatedServiceDate, filterRoutes);
    }

    private List<ActiveBlock> getUncachedActiveBlocks(Date serviceDate){
        List<AgencyAndId> filterRoutes = getVehicleAssignmentRoutes();
        return getUncachedActiveBlocks(serviceDate, filterRoutes);
    }

    private List<ActiveBlock> getUncachedActiveBlocks(Date serviceDate, List<AgencyAndId> filterRoutes){
        Map<String, ActiveBlock> activeBlocks = new HashMap<>();

        for (AgencyAndId agencyAndRouteId : filterRoutes) {
            _log.debug("agency and route id=" + agencyAndRouteId);
            RouteBean rb = _tds.getRouteForId(AgencyAndId.convertToString(agencyAndRouteId));
            if(rb !=null) {
                TimeZone tz = TimeZone.getTimeZone(rb.getAgency().getTimezone());
                long fromTime = getStartOfServiceDay(serviceDate).getTime();
                long toTime = getEndOfServiceDay(serviceDate).getTime();

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


    public Date getCurrentDate(){
        Date date = new Date();
        return date;
    }

    private Date getStartOfServiceDay(){
        return DateTimeUtil.getStartOfServiceDay(getCurrentDate());
    }

    private Date getStartOfServiceDay(Date date){
        return DateTimeUtil.getStartOfServiceDay(date);
    }

    /* Returns service date based on time of day.
       If current time is between 3:00 and 26:59:59.999, returns specified service date.
       Time < 3:00 returns previous day
       Time >= 27:00:00 returns tomorrow
     */
    private Date getAdjustedServiceDate(ServiceDate serviceDate){
        Calendar currentCalendar = Calendar.getInstance();
        TimeZone tz = currentCalendar.getTimeZone();

        Calendar serviceDateCalendar  = serviceDate.getAsCalendar(tz);
        serviceDateCalendar.set(serviceDateCalendar.HOUR_OF_DAY,  currentCalendar.get(Calendar.HOUR_OF_DAY));
        serviceDateCalendar.set(serviceDateCalendar.MINUTE,  currentCalendar.get(Calendar.MINUTE));
        serviceDateCalendar.set(serviceDateCalendar.SECOND,  currentCalendar.get(Calendar.SECOND));
        serviceDateCalendar.set(serviceDateCalendar.MILLISECOND,  currentCalendar.get(Calendar.MILLISECOND));

        Date startOfServiceDay = getStartOfServiceDay(serviceDateCalendar.getTime());

        return startOfServiceDay;
    }

    public void resetAssignments(){
        activeBlocksCache.invalidateAll();
        tripSummaryCache.invalidateAll();

        assignmentDao.deleteAll();
        assignmentConfigService.deleteConfigValue("lastUpdated");
    }

    private class CheckForBundleChange implements Runnable{
        @Override
        public void run() {

            String bundleId = assignmentConfigService.getConfigValueAsString("bundleId");
            String tdsBundleId = _tds.getActiveBundleId();

            if(bundleId == null){
                assignmentConfigService.setConfigValue("bundleId", tdsBundleId);
            }
            else{
                if(!bundleId.equalsIgnoreCase(tdsBundleId)){
                    resetAssignments();
                }
            }

        }
    }
}
