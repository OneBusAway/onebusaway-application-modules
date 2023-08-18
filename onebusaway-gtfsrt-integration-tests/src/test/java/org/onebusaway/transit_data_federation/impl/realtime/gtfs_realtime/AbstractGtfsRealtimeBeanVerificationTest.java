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
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.springframework.core.io.ClassPathResource;

/**
 * Do some basic tests of the response of arrivals-departures for the given stops.
 */
public abstract class AbstractGtfsRealtimeBeanVerificationTest extends AbstractGtfsRealtimeIntegrationTest {

    protected abstract String getPbFilename();
    protected abstract String getNorthStop();
    protected abstract String getSouthStop();

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
        StopEntry southStop = graph.getStopEntryForId(AgencyAndId.convertFromString(getSouthStop()));
        StopEntry northStop = graph.getStopEntryForId(AgencyAndId.convertFromString(getNorthStop()));
        long firstStopTime = source.getGtfsRealtimeTripLibrary().getCurrentTime();
        long window = 75 * 60 * 1000; // 75 minutes

        verifyBeans("northStop", northStop, firstStopTime);
        verifyBeans("southStop", southStop, firstStopTime);
    }
}
