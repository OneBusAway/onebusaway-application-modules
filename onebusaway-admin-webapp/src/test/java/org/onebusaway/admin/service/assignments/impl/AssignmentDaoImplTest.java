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

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onebusaway.admin.model.assignments.Assignment;
import org.onebusaway.admin.service.assignments.AssignmentDao;
import org.onebusaway.transit_data_federation.services.reporting.UserReportingDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@ContextConfiguration(locations = "classpath:org/onebusaway/admin/application-context-test.xml")
@TransactionConfiguration(transactionManager="transactionManager", defaultRollback=true)
@RunWith(SpringJUnit4ClassRunner.class)
public class AssignmentDaoImplTest {

    @Autowired
    private AssignmentDao _dao;

    Date currentDate = new Date();

    @Before
    public void setup() throws IOException {
        _dao.deleteAll();

    }

    private Date today(){
        return currentDate;
    }

    private Date yesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }


   @Test
    public void getAssignmentTest() {
        Assignment assignment = new Assignment("blockId", "vehicleId", today());

        Assignment retrievedAssignment = _dao.getAssignment("blockId", today());

        assertNull(retrievedAssignment);

        _dao.save(assignment);

        retrievedAssignment = _dao.getAssignment("blockId", today());

        assertEquals("blockId", retrievedAssignment.getBlockId());
        assertEquals("vehicleId", retrievedAssignment.getVehicleId());

        retrievedAssignment = _dao.getAssignment("blockId", yesterday());
        assertNull("blockId", retrievedAssignment);

    }

    @Test
    public void getAllTest() {

        List<Assignment> assignmentList = _dao.getAll(today());

        assertEquals(0, assignmentList.size());

        Assignment assignment = new Assignment("blockId", "vehicleId", today());

        _dao.save(assignment);

        assignmentList = _dao.getAll(today());

        assertEquals(1, assignmentList.size());
        Assignment retrievedAssignment = assignmentList.get(0);

        assertEquals("blockId", retrievedAssignment.getBlockId());
        assertEquals("vehicleId", retrievedAssignment.getVehicleId());

        assignmentList = _dao.getAll(yesterday());

        assertEquals(0, assignmentList.size());
    }

    @Test
    public void deleteTest() {
        Assignment retrievedAssignment = _dao.getAssignment("blockId", today());

        assertNull(retrievedAssignment);

        Assignment assignment = new Assignment("blockId", "vehicleId", today());

        _dao.save(assignment);

        retrievedAssignment = _dao.getAssignment("blockId", today());

        assertNotNull(retrievedAssignment);

        _dao.delete(retrievedAssignment);

        retrievedAssignment = _dao.getAssignment("blockId", today());

        assertNull(retrievedAssignment);
    }

    @Test
    public void deleteAllTest() {
        Assignment retrievedAssignment = _dao.getAssignment("blockId", today());

        assertNull(retrievedAssignment);

        Assignment assignment = new Assignment("blockId", "vehicleId", today());

        _dao.save(assignment);

        retrievedAssignment = _dao.getAssignment("blockId", today());

        assertNotNull(retrievedAssignment);

        _dao.deleteAll();

        retrievedAssignment = _dao.getAssignment("blockId", today());

        assertNull(retrievedAssignment);
    }

    @Test
    public void deleteAllExceptDateTest() {
        Assignment retrievedAssignment = _dao.getAssignment("blockId", today());

        assertNull(retrievedAssignment);

        Assignment assignment = new Assignment("blockId", "vehicleId", today());

        _dao.save(assignment);

        retrievedAssignment = _dao.getAssignment("blockId", today());

        assertNotNull(retrievedAssignment);

        _dao.deleteAllExceptDate(today());

        retrievedAssignment = _dao.getAssignment("blockId", today());

        assertNotNull(retrievedAssignment);

        retrievedAssignment = _dao.getAssignment("blockId", yesterday());

        assertNull(retrievedAssignment);

    }
}
