/*
 * Copyright 2008 Brian Ferris
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.washington.cs.rse.transit.web.oba.common.server;

import junit.framework.TestCase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import edu.washington.cs.rse.transit.MetroKCApplicationContext;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.OneBusAwayService;

public class OneBusAwayServiceImplTest extends TestCase {

    public OneBusAwayService _service;

    public OneBusAwayServiceImplTest() {
        ApplicationContext context = MetroKCApplicationContext.getApplicationContext();
        context.getAutowireCapableBeanFactory().autowireBean(this);
    }

    @Autowired
    public void setService(OneBusAwayService service) {
        _service = service;
    }

    public void testGetStopByRouteSerializedLength() throws Exception {
        _service.getArrivalsByStopId(2360);
    }
}
