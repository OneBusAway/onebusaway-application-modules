/**
 * Copyright (C) 2017 Metropolitan Transportation Authority
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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.OccupancyStatus;
import org.onebusaway.realtime.api.VehicleOccupancyListener;
import org.onebusaway.realtime.api.VehicleOccupancyRecord;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.VehicleStatusBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *  TEST CODE ONLY!  Consider this an example of how to integrate APC data.
 *
 * On a configurable interval, iterate through active vehicles in system
 * and update APC data for testing purposes.
 */
public class APCTestDataGenerator {

    private static Logger _log = LoggerFactory.getLogger(APCTestDataGenerator.class);

    @Autowired
    // if running in NYC uncomment the following line
    @Qualifier("nycTransitDataServiceImpl")
    private TransitDataService _tds;

    @Autowired
    private VehicleOccupancyListener _listener;

    private ScheduledExecutorService executor;
    private int _refreshIntervalSeconds = 60;

    @PostConstruct
    public void setup() {
        _log.info("init");
        // startup threads
        executor =
                Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(new GeneratorThread(), _refreshIntervalSeconds, _refreshIntervalSeconds, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void teardown() {
        _log.info("shutting down....");
        if (executor != null)
            executor.shutdownNow();
    }

    private class GeneratorThread extends TimerTask {
        @Override
        public void run() {
            long start = System.currentTimeMillis();
            try {
                _log.info("run");
                int count = 0;
                int populated = 0;
                int resets = 0;
                for (AgencyWithCoverageBean agenciesWithCoverage : _tds.getAgenciesWithCoverage()) {
                    for (VehicleStatusBean vehicle : _tds.getAllVehiclesForAgency(agenciesWithCoverage.getAgency().getId(), System.currentTimeMillis()).getList()) {
                        VehicleOccupancyRecord vor = generate(vehicle);
                        if (vor != null) {
                            _listener.handleVehicleOccupancyRecord(vor);
                            count++;
                            if (vor.getOccupancyStatus() != OccupancyStatus.UNKNOWN) {
                                populated++;
                            }
                        } else {
                            _listener.resetVehicleOccupancy(AgencyAndId.convertFromString(vehicle.getVehicleId()));
                            resets++;
                        }
                    }

                }
                _log.info("run exiting with " + count + " handled, " + populated + " valid and " + resets + " reset");
            } catch (Exception any) {
                _log.error("run failed:", any);
            } finally {
                _log.info("run complete in " + (System.currentTimeMillis() - start) + "ms");
            }
        }

        private VehicleOccupancyRecord generate(VehicleStatusBean vsb) {
            if (vsb == null) {
                return null;
            }
            if (vsb.getTripStatus() == null) {
                return null;
            }
            if (!vsb.getTripStatus().isPredicted()) {
                return null;
            }
            if (vsb.getTripStatus().getActiveTrip() == null) {
                return null;
            }

            VehicleOccupancyRecord vor = new VehicleOccupancyRecord();
            vor.setVehicleId(AgencyAndId.convertFromString(vsb.getVehicleId()));

            double distanceAlongTrip = vsb.getTripStatus().getDistanceAlongTrip();
            double totalDistance = vsb.getTripStatus().getActiveTrip().getTotalTripDistance();
            if (totalDistance <= 0.0 || totalDistance < distanceAlongTrip) {
                // we are outside of trip bounds, bus is empty
                vor.setOccupancyStatus(OccupancyStatus.UNKNOWN);
            } else {
                vor.setOccupancyStatus(mapPercentageToEnumeration(distanceAlongTrip/totalDistance));
            }
            return vor;
        }

        /**
         * here we generate fake data based on the progress of the bus along the trip;
         * buses early (distance wise) in their trip are empty, buses late in their
         * trip are full.
         */
        private OccupancyStatus mapPercentageToEnumeration(double percent) {
            if (percent > 0.90)
                return OccupancyStatus.NOT_ACCEPTING_PASSENGERS;
            if (percent > 0.80)
                return OccupancyStatus.CRUSHED_STANDING_ROOM_ONLY;
            if (percent > 0.70)
                return OccupancyStatus.FULL;
            if (percent > 0.50)
                return OccupancyStatus.FEW_SEATS_AVAILABLE;
            if (percent > 0.30)
                return OccupancyStatus.MANY_SEATS_AVAILABLE;
            return OccupancyStatus.EMPTY;
        }
    }
}
