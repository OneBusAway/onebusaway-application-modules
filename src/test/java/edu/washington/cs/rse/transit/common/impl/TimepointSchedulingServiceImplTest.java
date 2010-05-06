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
package edu.washington.cs.rse.transit.common.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import edu.washington.cs.rse.transit.MetroKCApplicationContext;
import edu.washington.cs.rse.transit.common.model.aggregate.ScheduledArrivalTime;
import edu.washington.cs.rse.transit.common.services.NoSuchStopException;
import edu.washington.cs.rse.transit.common.services.TimepointSchedulingService;

public class TimepointSchedulingServiceImplTest extends TestCase {

    private static DateFormat _format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    private TimepointSchedulingService _service;

    public TimepointSchedulingServiceImplTest() {
        ApplicationContext context = MetroKCApplicationContext.getApplicationContext(true);
        context.getAutowireCapableBeanFactory().autowireBean(this);
    }

    @Autowired
    public void setTimepointSchedulingService(TimepointSchedulingService service) {
        _service = service;
    }

    public void testSingleTimepoint() throws IOException, NoSuchStopException {
        long t = parseDate("7/7/08 11:41 PM");
        List<ScheduledArrivalTime> sats = _service.getPredictedArrivalsByTimepointId(5518, t);
        assertEquals(6, sats.size());
    }

    public void atestMultipleTimepoints() throws IOException, NoSuchStopException {
        Set<Integer> ids = new HashSet<Integer>();
        int[] idArray = { 309, 501, 4318, 4310, 1637, 4355, 4304, 419, 406, 1638, 402, 1634, 220, 411, 623, 316 };
        for (int id : idArray)
            ids.add(id);
    }

    private long parseDate(String date) {
        try {
            return _format.parse(date).getTime();
        } catch (ParseException e) {
            fail();
            return -1;
        }

    }
}
