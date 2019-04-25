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
import org.onebusaway.admin.model.assignments.AssignmentDate;
import org.onebusaway.admin.service.assignments.AssignmentDao;
import org.onebusaway.admin.service.assignments.AssignmentDateDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@ContextConfiguration(locations = "classpath:org/onebusaway/admin/application-context-test.xml")
@TransactionConfiguration(transactionManager="transactionManager", defaultRollback=true)
@RunWith(SpringJUnit4ClassRunner.class)
public class AssignmentDateDaoImplTest {

    @Autowired
    private AssignmentDateDao _dao;

    @Before
    public void setup() throws IOException {
        _dao.deleteAll();
    }

    @Test
    @Transactional
    public void getAssignmentTest() {
        Date date = new Date();

        AssignmentDate assignmentDate = new AssignmentDate("lastUpdatedTest", date);

        AssignmentDate retrievedAssignmentDate = _dao.getAssignmentDate("lastUpdatedTest");

        assertNull(retrievedAssignmentDate);

        _dao.save(assignmentDate);

        retrievedAssignmentDate = _dao.getAssignmentDate("lastUpdatedTest");

        assertEquals(date, retrievedAssignmentDate.getValue());

    }

    @Test
    @Transactional
    public void getAllTest() {

        Date date = new Date();

        List<AssignmentDate> assignmentDateList = _dao.getAll();

        assertEquals(0, assignmentDateList.size());

        AssignmentDate assignmentDate = new AssignmentDate("lastUpdatedTest", date);

        _dao.save(assignmentDate);

        assignmentDateList = _dao.getAll();

        assertEquals(1, assignmentDateList.size());
        AssignmentDate retrievedAssignmentDate = assignmentDateList.get(0);

        assertEquals(date, retrievedAssignmentDate.getValue());

    }

    @Test
    public void deleteTest() {
        Date date = new Date();

        AssignmentDate retrievedAssignmentDate = _dao.getAssignmentDate("lastUpdatedTest");

        assertNull(retrievedAssignmentDate);

        AssignmentDate assignmentDate = new AssignmentDate("lastUpdatedTest", date);

        _dao.save(assignmentDate);

        retrievedAssignmentDate = _dao.getAssignmentDate("lastUpdatedTest");

        assertNotNull(retrievedAssignmentDate);

        _dao.delete(retrievedAssignmentDate);

        retrievedAssignmentDate = _dao.getAssignmentDate("lastUpdatedTest");

        assertNull(retrievedAssignmentDate);
    }

    @Test
    public void deleteAllTest() {
        Date date = new Date();

        AssignmentDate retrievedAssignmentDate = _dao.getAssignmentDate("lastUpdatedTest");

        assertNull(retrievedAssignmentDate);

        AssignmentDate assignmentDate = new AssignmentDate("lastUpdatedTest", date);

        _dao.save(assignmentDate);

        retrievedAssignmentDate = _dao.getAssignmentDate("lastUpdatedTest");

        assertNotNull(retrievedAssignmentDate);

        _dao.deleteAll();

        retrievedAssignmentDate = _dao.getAssignmentDate("lastUpdatedTest");

        assertNull(retrievedAssignmentDate);
    }
}
