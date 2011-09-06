/**
 * Copyright (c) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.transit_data_federation.bundle.tasks.stif;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.onebusaway.transit_data_federation.bundle.tasks.stif.model.GeographyRecord;
import org.onebusaway.transit_data_federation.bundle.tasks.stif.model.ServiceCode;
import org.onebusaway.transit_data_federation.bundle.tasks.stif.model.SignCodeRecord;
import org.onebusaway.transit_data_federation.bundle.tasks.stif.model.StifRecord;
import org.onebusaway.transit_data_federation.bundle.tasks.stif.model.TimetableRecord;
import org.onebusaway.transit_data_federation.bundle.tasks.stif.model.TripRecord;

public class TestStifRecordReader {
	@Test
	public void testRecordReader() throws IOException {
	      InputStream in = getClass().getResourceAsStream("stif.m_0014__.210186.sun");
	      StifRecordReader reader = new StifRecordReader(in);
	      StifRecord record = reader.read();
	      assertNotNull(record);
	      assertTrue(record instanceof TimetableRecord);
	      TimetableRecord timetable = (TimetableRecord) record;
	      assertSame(ServiceCode.SUNDAY, timetable.getServiceCode());
	      assertEquals("0014", timetable.getRouteIdentifier());
	      for (int i = 0; i < 86; ++i) { 
	    	  record = reader.read();
	    	  GeographyRecord stop = (GeographyRecord) record;
	    	  assertNotNull(stop.getBoxID());
	    	  assertTrue(stop.getLongitude() > -74.9);
	    	  assertTrue(stop.getLongitude() < -73.0);
	      }
	      record = reader.read();
	      TripRecord trip = (TripRecord) record;
	      assertEquals("11559238", trip.getBlockNumber());
	      assertEquals("M14AD", trip.getRoute());
	      StifRecord lastRecord = record;
	      while (record != null) {
	    	  lastRecord = record;
	    	  record = reader.read();
		}
		assertTrue(lastRecord instanceof SignCodeRecord);
	}
}
