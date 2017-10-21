/**
 * Copyright (C) 2017 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.api;

import org.onebusaway.admin.service.impl.NotificationServiceImpl;
import org.onebusaway.presentation.impl.service_alerts.NotificationStrategy;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;


import org.junit.Test;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Test some of the helper methods in notification resource
 */
public class NotifyResourceTest {


    @Test
    public void testToTweet() {
        ServiceAlertBean bean = null;

        NotificationServiceImpl nsi = new NotificationServiceImpl();
        NotificationStrategy ns = new TestNotificationStrategy();
        nsi.setNotificationStrategy(ns);

        NotifyResource resource = new NotifyResource();
        resource.setNotificationService(nsi);

        assertNull(resource.toTweet(bean));

        bean = new ServiceAlertBean();
        assertNull(resource.toTweet(bean));
        bean.setSummaries(new ArrayList<NaturalLanguageStringBean>());
        bean.getSummaries().add(createNLS("Snow Routes in Affect"));

        // service alert has no affects clause, nothing to do
        assertEquals(null, resource.toTweet(bean));

        SituationAffectsBean affects = new SituationAffectsBean();
        affects.setAgencyId("ACTA");
        bean.setAllAffects(new ArrayList<SituationAffectsBean>());
        bean.getAllAffects().add(affects);

        // we don't include agency in tweet -- it will be obvious from the twitter handle
        assertEquals("Snow Routes in Affect", resource.toTweet(bean));

        // add a single route
        affects.setRouteId("A1");
        assertEquals("Snow Routes in Affect affecting route(s) ACTA_A1", resource.toTweet(bean));

        // add a stop
        affects = new SituationAffectsBean();
        affects.setAgencyId("ACTA");
        bean.getAllAffects().add(affects);
        affects.setStopId("Water and Blowers");

        try {
            assertEquals("Snow Routes in Affect affecting route(s) A1 and stop(s) Water and Blowers", resource.toTweet(bean));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            // pass
        }

        affects.setStopId("ACTA_6968");
        assertEquals("Snow Routes in Affect affecting route(s) ACTA_A1 and stop(s) ACTA_6968", resource.toTweet(bean));

        // add another route
        affects = new SituationAffectsBean();
        affects.setAgencyId("ACTA");

        bean.getAllAffects().add(affects);
        affects.setRouteId("B2");
        assertEquals("Snow Routes in Affect affecting route(s) ACTA_A1, ACTA_B2 and stop(s) ACTA_6968", resource.toTweet(bean));

        // add another stop
        affects = new SituationAffectsBean();
        affects.setAgencyId("ACTA");
        bean.getAllAffects().add(affects);
        affects.setStopId("ACTA_4370");
        assertEquals("Snow Routes in Affect affecting route(s) ACTA_A1, ACTA_B2 and stop(s) ACTA_6968, ACTA_4370", resource.toTweet(bean));

        // clear out routes, add a single stop
        affects = new SituationAffectsBean();
        affects.setAgencyId("ACTA");
        bean.setAllAffects(new ArrayList<SituationAffectsBean>());
        bean.getAllAffects().add(affects);
        affects.setStopId("ACTA_4370");
        assertEquals("Snow Routes in Affect affecting stop(s) ACTA_4370", resource.toTweet(bean));

        // add another stop
        affects = new SituationAffectsBean();
        affects.setAgencyId("ACTA");
        bean.getAllAffects().add(affects);
        affects.setStopId("ACTA_6968");
        assertEquals("Snow Routes in Affect affecting stop(s) ACTA_4370, ACTA_6968", resource.toTweet(bean));

        // we don't support trip level tweets

    }

    private NaturalLanguageStringBean createNLS(String msg) {
        NaturalLanguageStringBean bean = new NaturalLanguageStringBean();
        bean.setLang("en");
        bean.setValue(msg);
        return bean;
    }

    public static class TestNotificationStrategy implements NotificationStrategy {

        @Override
        public String summarizeRoute(String routeId) {
            return routeId;
        }

        @Override
        public String summarizeStop(String stopId) {
            return stopId;
        }
    }
}
