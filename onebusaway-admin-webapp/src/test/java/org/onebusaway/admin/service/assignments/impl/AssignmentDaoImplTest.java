/**
 * Copyright (C) 2019 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.assignments.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onebusaway.admin.model.assignments.Assignment;
import org.onebusaway.admin.service.assignments.AssignmentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@ContextConfiguration(locations = "classpath:org/onebusaway/admin/application-context-test.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class AssignmentDaoImplTest {

    @Autowired
    private AssignmentDao _dao;

    Date currentDate = new Date();

    @Before
    public void setup() throws IOException {
        _dao.deleteAll();

    }

    private Date currentServiceDate(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);

        cal.set(Calendar.HOUR_OF_DAY, 3);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 000);
        return cal.getTime();
    }

    private Date previousServiceDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);

        cal.set(Calendar.HOUR_OF_DAY, 2);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 000);
        return cal.getTime();
    }

    private Date laterPreviousServiceDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);

        cal.set(Calendar.HOUR_OF_DAY, 2);
        cal.set(Calendar.MINUTE, 10);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 000);
        return cal.getTime();
    }


   @Test
    public void getAssignmentTest() {
        Date currentServiceDate = currentServiceDate();
        Date previousServiceDate = previousServiceDate();

        Assignment assignment = new Assignment("blockId", "vehicleId", currentServiceDate);

        Assignment retrievedAssignment = _dao.getAssignment("blockId", currentServiceDate);

        assertNull(retrievedAssignment);

        _dao.save(assignment);

        retrievedAssignment = _dao.getAssignment("blockId", currentServiceDate);

        assertEquals("blockId", retrievedAssignment.getBlockId());
        assertEquals("vehicleId", retrievedAssignment.getVehicleId());

        retrievedAssignment = _dao.getAssignment("blockId", previousServiceDate);
        assertNull("blockId", retrievedAssignment);

    }

    @Test
    public void getAssignmentServiceDayChangeTest() {
        Date currentServiceDate = currentServiceDate();
        Date previousServiceDate = previousServiceDate();
        Date laterPreviousServiceDate = laterPreviousServiceDate();

        Assignment assignment = new Assignment("blockId", "vehicleId", previousServiceDate);

        // Check to make sure that we still get assignment with same service date but different time
        Assignment retrievedAssignment = _dao.getAssignment("blockId", laterPreviousServiceDate);

        assertNull(retrievedAssignment);

        _dao.save(assignment);

        retrievedAssignment = _dao.getAssignment("blockId", laterPreviousServiceDate);

        assertEquals("blockId", retrievedAssignment.getBlockId());
        assertEquals("vehicleId", retrievedAssignment.getVehicleId());

        retrievedAssignment = _dao.getAssignment("blockId", currentServiceDate);
        assertNull("blockId", retrievedAssignment);

    }

    @Test
    public void getAllTest() {

        Date currentServiceDate = currentServiceDate();
        Date previousServiceDate = previousServiceDate();

        List<Assignment> assignmentList = _dao.getAll(currentServiceDate());

        assertEquals(0, assignmentList.size());

        Assignment assignment = new Assignment("blockId", "vehicleId", currentServiceDate);

        _dao.save(assignment);

        assignmentList = _dao.getAll(currentServiceDate());

        assertEquals(1, assignmentList.size());
        Assignment retrievedAssignment = assignmentList.get(0);

        assertEquals("blockId", retrievedAssignment.getBlockId());
        assertEquals("vehicleId", retrievedAssignment.getVehicleId());

        assignmentList = _dao.getAll(previousServiceDate);

        assertEquals(0, assignmentList.size());
    }

    @Test
    public void deleteTest() {

        Date currentServiceDate = currentServiceDate();

        Assignment retrievedAssignment = _dao.getAssignment("blockId", currentServiceDate);

        assertNull(retrievedAssignment);

        Assignment assignment = new Assignment("blockId", "vehicleId", currentServiceDate);

        _dao.save(assignment);

        retrievedAssignment = _dao.getAssignment("blockId", currentServiceDate);

        assertNotNull(retrievedAssignment);

        _dao.delete(retrievedAssignment);

        retrievedAssignment = _dao.getAssignment("blockId", currentServiceDate);

        assertNull(retrievedAssignment);
    }

    @Test
    public void deleteAllTest() {

        Date currentServiceDate = currentServiceDate();

        Assignment retrievedAssignment = _dao.getAssignment("blockId", currentServiceDate);

        assertNull(retrievedAssignment);

        Assignment assignment = new Assignment("blockId", "vehicleId", currentServiceDate);

        _dao.save(assignment);

        retrievedAssignment = _dao.getAssignment("blockId", currentServiceDate);

        assertNotNull(retrievedAssignment);

        _dao.deleteAll();

        retrievedAssignment = _dao.getAssignment("blockId", currentServiceDate);

        assertNull(retrievedAssignment);
    }

}
