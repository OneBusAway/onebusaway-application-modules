/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.bundle.api;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.admin.service.bundle.api.BuildResource;
import org.onebusaway.admin.service.exceptions.DateValidationException;

public class BuildResourceTest {

	private BuildResource buildResource;
	
	@Before
	public void setUp() {
		buildResource = new BuildResource();
	}
	
	@Test
	public void testInvalidDates() {
		try {
			buildResource.validateDates("2012-06-26", "2012-06-21");
		} catch(DateValidationException e) {
			assertEquals("Start date: " + "2012-06-26" + " should be before End date: " + "2012-06-21", e.getMessage());
		}
	}
	
	@Test
	public void testEmptyDates() {
		try {
			buildResource.validateDates("2012-06-26", " ");
		} catch(DateValidationException e) {
			assertEquals("End date cannot be empty", e.getMessage());
		}
		try {
			buildResource.validateDates(" ", "2012-06-25");
		} catch(DateValidationException e) {
			assertEquals("Start date cannot be empty", e.getMessage());
		}
	}
	
	@Test
	public void testValidDates() {
		try {
			buildResource.validateDates("2012-06-21", "2012-06-26");
		} catch(DateValidationException e) {
			//This should never happen
			e.printStackTrace();
		}
	}

}
