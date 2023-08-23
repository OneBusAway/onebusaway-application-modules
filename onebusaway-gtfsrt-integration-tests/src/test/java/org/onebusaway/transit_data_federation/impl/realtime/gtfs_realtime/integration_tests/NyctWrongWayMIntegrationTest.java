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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.integration_tests;

import org.junit.Test;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.AbstractGtfsRealtimeBeanVerificationTest;

import java.util.HashMap;
import java.util.Map;

public class NyctWrongWayMIntegrationTest extends AbstractGtfsRealtimeBeanVerificationTest {
    @Override
    protected String getIntegrationTestPath() {
        return "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/nyct_wrong_way";
    }

    @Override
    protected String[] getPaths() {
        String[] paths = {"test-data-sources.xml"};
        return paths;
    }

    @Override
    protected String getPbFilename() {
        return "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/nyct_wrong_way/m.pb";
    }

    @Override
    protected String[] getNorthStops() {
        String[] a = {"MTASBWY_M01N", "MTASBWY_M04N", "MTASBWY_M10N", "MTASBWY_M16N", "MTASBWY_M18N"};
        return a;
    }

    @Override
    protected String[] getSouthStops() {
        String[] a = {"MTASBWY_M01S", "MTASBWY_M04S", "MTASBWY_M10S", "MTASBWY_M16S", "MTASBWY_M18S"};
        return a;
    }

    protected Map<String, String> getStopToHeadsignMap() {
        Map<String, String> map = new HashMap();
        map.put("MTASBWY_M01N.1", "Manhattan");
        map.put("MTASBWY_M01N.0", "Manhattan");
        map.put("MTASBWY_M01S.0", "Metropolitan Av");
        map.put("MTASBWY_M01S.1", "Metropolitan Av");
        map.put("MTASBWY_M04S.1", "Manhattan");
        map.put("MTASBWY_M04S.0", "Manhattan");
        map.put("MTASBWY_M04S.0", "Metropolitan Av");
        map.put("MTASBWY_M04S.1", "Metropolitan Av");
        map.put("MTASBWY_M10N.1", "Manhattan");
        map.put("MTASBWY_M10N.0", "Manhattan");
        map.put("MTASBWY_M10S.0", "Metropolitan Av");
        map.put("MTASBWY_M10S.1", "Metropolitan Av");
        //GTFS:  M16S "Manhattan", direction = 1 (S)
        //GTFS:  M16N "Jamaica & Middle Village", direction = 0 (N)
        map.put("MTASBWY_M16N.1", "Jamaica & Middle Village"); // this is a wrong way concurrency
        map.put("MTASBWY_M16N.0", "Jamaica & Middle Village"); // this is GTFS supported
        // now M16S
        map.put("MTASBWY_M16S.0", "Manhattan"); // this is a wrong way concurrency
        map.put("MTASBWY_M16S.1", "Manhattan"); // this is GTFS supported

        map.put("MTASBWY_M18N.1", "Brooklyn");
        map.put("MTASBWY_M18N.0", "Brooklyn");
        map.put("MTASBWY_M18S.0", "Broad St (JZ) - Uptown (M)");
        map.put("MTASBWY_M18S.1", "Broad St (JZ) - Uptown (M)");

        map.put("MTASBWY_J31N.1", "Jamaica");
        map.put("MTASBWY_J31N.0", "Jamaica");
        map.put("MTASBWY_J31S.0", "Manhattan");
        map.put("MTASBWY_J31S.1", "Manhattan");

        return map;
    }

    @Test
    public void testWrongWayConcurrenciesOnJM() throws Exception {
        executeTest();
    }

}
