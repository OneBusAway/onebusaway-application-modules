package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.integration_tests;

import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.transit_data_federation.impl.realtime.TestVehicleLocationListener;
import org.onebusaway.transit_data_federation.impl.realtime.VehicleStatusServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.AbstractGtfsRealtimeIntegrationTest;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.springframework.core.io.ClassPathResource;

public class NyctWrongWayMIntegrationTest extends AbstractGtfsRealtimeIntegrationTest {
    @Override
    protected String getIntegrationTestPath() {
        return "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/nyct_wrong_way";
    }

    @Override
    protected String[] getPaths() {
        String[] paths = {"test-data-sources.xml"};
        return paths;
    }

    @Test
    public void testWrongWayConcurrenciesOnJM() throws Exception {
        GtfsRealtimeSource source = getBundleLoader().getSource();
        source.setAgencyId("MTASBWY");

        TestVehicleLocationListener listener = new TestVehicleLocationListener();

        VehicleLocationListener actualListener = getBundleLoader().getApplicationContext().getBean(VehicleStatusServiceImpl.class);
        listener.setVehicleLocationListener(actualListener);
        source.setVehicleLocationListener(listener);

        // this is the gtfs-rt protocol-buffer file to match to the loaded bundle
        String gtfsrtFilename = "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/nyct_wrong_way/m.pb";
        ClassPathResource gtfsRtResource = new ClassPathResource(gtfsrtFilename);
        if (!gtfsRtResource.exists()) throw new RuntimeException(gtfsrtFilename + " not found in classpath!");
        source.setTripUpdatesUrl(gtfsRtResource.getURL());
        source.refresh(); // launch

        TransitGraphDao graph = getBundleLoader().getApplicationContext().getBean(TransitGraphDao.class);
        StopEntry southStop = graph.getStopEntryForId(AgencyAndId.convertFromString("MTASBWY_M16S"));
        StopEntry northStop = graph.getStopEntryForId(AgencyAndId.convertFromString("MTASBWY_M16N"));
        long firstStopTime = source.getGtfsRealtimeTripLibrary().getCurrentTime();
        long window = 75 * 60 * 1000; // 75 minutes

        verifyBeans("northStop", northStop, firstStopTime);
        verifyBeans("southStop", southStop, firstStopTime);

    }

}
