package org.onebusaway.admin.service.impl;

import org.onebusaway.admin.service.VehicleAssignmentService;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.blocks.BlockBean;
import org.onebusaway.transit_data.model.blocks.BlockInstanceBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
    public List<BlockBean> getActiveBlocks(ServiceDate serviceDate, List<AgencyAndId> filterRoutes) {
        Set<String> filteredBlockIds = new HashSet<>();
        for (AgencyAndId aid : filterRoutes) {
            _log.debug("agency id=" + aid);
            ListBean<RouteBean> routesForAgencyId = _tds.getRoutesForAgencyId(aid.getAgencyId());
            _log.debug("routes for agency = " + routesForAgencyId.getList());
            for (RouteBean rb : routesForAgencyId.getList()) {
                TripsForRouteQueryBean query = new TripsForRouteQueryBean();
                query.setRouteId(rb.getId());
                ListBean<TripDetailsBean> tripsForRoute = _tds.getTripsForRoute(query);
                _log.debug("trips for route " + rb.getId() + " = " + tripsForRoute);
                for (TripDetailsBean tdb : tripsForRoute.getList()) {
                    TimeZone tz = TimeZone.getTimeZone(rb.getAgency().getTimezone());
                    if (isActive(tdb.getTrip().getBlockId(), serviceDate.getAsDate().getTime())) {
                        _log.debug(tdb.getTrip().getBlockId() + " is active!");
                        filteredBlockIds.add(tdb.getTrip().getBlockId());
                    } else {
                        _log.debug(tdb.getTrip().getBlockId() + " is NOT active!");
                    }
                }
            }
        }
        List<BlockBean> activeBlocks = new ArrayList<>(filteredBlockIds.size());
        for (String blockId : filteredBlockIds) {
            activeBlocks.add(_tds.getBlockForId(blockId));
        }
        return activeBlocks;
    }

    private boolean isActive(String blockId, long serviceDate) {
        // to determine if we are active or not we try to load the block on the given service data
        BlockInstanceBean blockInstance = _tds.getBlockInstance(blockId, serviceDate);
        if (blockInstance == null) {
            _log.info("discarding block " + blockId + " because it is not active on " + new Date(serviceDate));
            return false;
        }
        return true;
    }

    @Override
    public Map<String, String> getAssignments() {
        Map<String, String> sortedMap = new TreeMap<String, String>();
        sortedMap.putAll(_blockAssignments);
        return sortedMap;
    }

    private boolean insert(String blockId, String vehicleId) {
        _blockAssignments.put(blockId, vehicleId);
        return true;
    }
}
