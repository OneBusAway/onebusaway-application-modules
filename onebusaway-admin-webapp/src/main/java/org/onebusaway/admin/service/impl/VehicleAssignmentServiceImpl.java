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
package org.onebusaway.admin.service.impl;

import org.onebusaway.admin.model.ActiveBlock;
import org.onebusaway.admin.service.VehicleAssignmentService;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.blocks.BlockInstanceBean;
import org.onebusaway.transit_data.model.blocks.BlockStopTimeBean;
import org.onebusaway.transit_data.model.blocks.BlockTripBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Component
/**
 * Manage manual vehicle assignments.  Currently in memory only.
 */
public class VehicleAssignmentServiceImpl implements VehicleAssignmentService {

    private static Logger _log = LoggerFactory.getLogger(VehicleAssignmentServiceImpl.class);

    @Autowired
    private TransitDataService _tds;
    public void setTransitDataService(TransitDataService tds) {
        _tds = tds;
    }

    private ConcurrentMap<String, String> _blockAssignments = new ConcurrentHashMap<>();


    @Override
    public boolean assign(String blockId, String vehicleId) {
        return insert(blockId, vehicleId);
    }

    @Override
    public String getAssignmentByBlockId(String blockId) {
        return _blockAssignments.get(blockId);
    }

    @Override
    public Map<String, String> getAssignments() {
        Map<String, String> sortedMap = new TreeMap<String, String>();
        sortedMap.putAll(_blockAssignments);
        return sortedMap;
    }

    @Override
    public List<ActiveBlock> getActiveBlocks(ServiceDate serviceDate, List<AgencyAndId> filterRoutes) {

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
        _blockAssignments.put(blockId, vehicleId);
        return true;
    }
}
