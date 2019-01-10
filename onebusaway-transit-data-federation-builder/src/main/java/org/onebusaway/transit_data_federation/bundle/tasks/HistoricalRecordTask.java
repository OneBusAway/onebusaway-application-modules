/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.bundle.tasks;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Ridership;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.model.bundle.HistoricalRidership;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * load Ridership data (from ridership.txt) into HistoricalRidership Bundle Index
 */
public class HistoricalRecordTask implements Runnable {
    private FederatedTransitDataBundle _bundle;
    private GtfsRelationalDao _gtfsDao;
    private Logger _log = LoggerFactory.getLogger(HistoricalRecordTask.class);

    @Autowired
    public void setBundle(FederatedTransitDataBundle bundle) {
        _bundle = bundle;
    }

    @Autowired
    public void setGtfsDao(GtfsRelationalDao gtfsDao) {
        _gtfsDao = gtfsDao;
    }

    @Override
    public void run() {
        try {
            _log.info("gtfsDao has " + _gtfsDao.getAllRiderships().size() + " records ");
            List<HistoricalRidership> historicalRiderships = new ArrayList<>();
            for (Ridership r : _gtfsDao.getAllRiderships()) {
                historicalRiderships.add(toHistoricalRidership(r));
            }
            _log.info("translated " + historicalRiderships.size() + " records ");
            ObjectSerializationLibrary.writeObject(_bundle.getHistoricalRidershipPath(), historicalRiderships);
            _log.info("wrote " + historicalRiderships.size() + " records to " + _bundle.getHistoricalRidershipPath());
        } catch (Exception ex) {
            _log.error("fatal exception building HistoricalRecordTask:", ex);
        }
    }

    private HistoricalRidership toHistoricalRidership(Ridership r) {
        HistoricalRidership.Builder hr = HistoricalRidership.builder();
        hr.setStopId(findStopId(r.getStopId()));
        hr.setTripId(findTripId(r.getTripId()));
        hr.setRouteId(findRouteId(r.getRouteId()));
        hr.setCalendarType(HistoricalRidership.CalendarType.WEEKDAY);
        hr.setLoadFactor(r.getAverageLoad());
        _log.info("created Ridership " + r.getRouteId() + ":" + r.getTripId() + ":" + r.getStopId() + "=" + r.getAverageLoad());
        return hr.create();
    }

    private AgencyAndId findStopId(String rawStopId) {
        //TODO search through GTFS to find agency_id
        return new AgencyAndId("1", rawStopId);
    }
    private AgencyAndId findTripId(String rawTripId) {
        //TODO search through GTFS to find agency_id
        return new AgencyAndId("1", rawTripId);
    }
    private AgencyAndId findRouteId(String rawRouteId) {
        //TODO search through GTFS to find agency_id
        return new AgencyAndId("1", rawRouteId);
    }

}
