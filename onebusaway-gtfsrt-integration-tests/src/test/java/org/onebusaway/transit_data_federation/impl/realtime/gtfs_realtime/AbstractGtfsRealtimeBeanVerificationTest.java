/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.transit_data_federation.impl.realtime.TestVehicleLocationListener;
import org.onebusaway.transit_data_federation.impl.realtime.VehicleStatusServiceImpl;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.springframework.core.io.ClassPathResource;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Do some basic tests of the response of arrivals-departures for the given stops.
 */
public abstract class AbstractGtfsRealtimeBeanVerificationTest extends AbstractGtfsRealtimeIntegrationTest {

    protected abstract String getPbFilename();
    protected abstract String[] getNorthStops();
    protected abstract String[] getSouthStops();
    protected Map<String, String> getStopToHeadsignMap() {
        return null;
    }



    public void executeTest() throws Exception {
        GtfsRealtimeSource source = getBundleLoader().getSource();
        source.setAgencyId("MTASBWY");

        TestVehicleLocationListener listener = new TestVehicleLocationListener();

        VehicleLocationListener actualListener = getBundleLoader().getApplicationContext().getBean(VehicleStatusServiceImpl.class);
        listener.setVehicleLocationListener(actualListener);
        source.setVehicleLocationListener(listener);

        // this is the gtfs-rt protocol-buffer file to match to the loaded bundle
        String gtfsrtFilename = getPbFilename();
        ClassPathResource gtfsRtResource = new ClassPathResource(gtfsrtFilename);
        if (!gtfsRtResource.exists()) throw new RuntimeException(gtfsrtFilename + " not found in classpath!");
        source.setTripUpdatesUrl(gtfsRtResource.getURL());
        source.refresh(); // launch

        TransitGraphDao graph = getBundleLoader().getApplicationContext().getBean(TransitGraphDao.class);
        long firstStopTime = source.getGtfsRealtimeTripLibrary().getCurrentTime();
        long window = 75 * 60 * 1000; // 75 minutes

        for (String southStopId : getSouthStops()) {
            StopEntry southStop = graph.getStopEntryForId(AgencyAndId.convertFromString(southStopId));
            assertNotNull(southStop);
            verifyBeans("southStop", southStop, firstStopTime);
        }

        for (String northStopId : getNorthStops()) {
            StopEntry northStop = graph.getStopEntryForId(AgencyAndId.convertFromString(northStopId));
            assertNotNull(northStop);
            verifyBeans("northStop", northStop, firstStopTime);
        }
    }
    public void validateHeadsign(StopTimeEntry stopTimeEntry, StopTimeNarrative stopTimeNarrative) {
        Map<String, String> stopToHeadsign = getStopToHeadsignMap();
        if (stopToHeadsign == null || stopToHeadsign.isEmpty()) return;
        String stopId = AgencyAndId.convertToString(stopTimeEntry.getStop().getId());
        String directionId = stopTimeEntry.getTrip().getDirectionId();
        String expectedHeadsign = stopToHeadsign.get(stopId + "." + directionId);
        if (expectedHeadsign == null) return;
        // if we have a headsign compare it to the narrative
        assertEquals("stop " + stopId + " and direction " + directionId
                + " on route " + stopTimeEntry.getTrip().getRoute().getId()
                + " expected ", expectedHeadsign, stopTimeNarrative.getStopHeadsign());
    }

}
