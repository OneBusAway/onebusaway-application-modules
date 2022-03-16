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
import org.onebusaway.admin.model.assignments.AssignmentConfig;
import org.onebusaway.admin.service.assignments.AssignmentConfigDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@ContextConfiguration(locations = "classpath:org/onebusaway/admin/application-context-test.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class AssignmentConfigDaoImplTest {

    @Autowired
    private AssignmentConfigDao _dao;

    @Before
    public void setup() throws IOException {
        _dao.deleteAll();
    }

    @Test
    @Transactional
    public void getAssignmentTest() {
        Date date = new Date();

        AssignmentConfig assignmentConfig = new AssignmentConfig("lastUpdatedTest", getDateAsString(date));

        AssignmentConfig retrievedAssignmentConfig = _dao.getAssignmentConfig("lastUpdatedTest");

        assertNull(retrievedAssignmentConfig);

        _dao.save(assignmentConfig);

        retrievedAssignmentConfig = _dao.getAssignmentConfig("lastUpdatedTest");

        assertEquals(getDateAsString(date), retrievedAssignmentConfig.getValue());

    }

    @Test
    @Transactional
    public void getAllTest() {

        Date date = new Date();

        List<AssignmentConfig> assignmentConfigList = _dao.getAll();

        assertEquals(0, assignmentConfigList.size());

        AssignmentConfig assignmentConfig = new AssignmentConfig("lastUpdatedTest", getDateAsString(date));

        _dao.save(assignmentConfig);

        assignmentConfigList = _dao.getAll();

        assertEquals(1, assignmentConfigList.size());
        AssignmentConfig retrievedAssignmentConfig = assignmentConfigList.get(0);

        assertEquals(getDateAsString(date), retrievedAssignmentConfig.getValue());

    }

    @Test
    public void deleteTest() {
        Date date = new Date();

        AssignmentConfig retrievedAssignmentConfig = _dao.getAssignmentConfig("lastUpdatedTest");

        assertNull(retrievedAssignmentConfig);

        AssignmentConfig assignmentConfig = new AssignmentConfig("lastUpdatedTest", getDateAsString(date));

        _dao.save(assignmentConfig);

        retrievedAssignmentConfig = _dao.getAssignmentConfig("lastUpdatedTest");

        assertNotNull(retrievedAssignmentConfig);

        _dao.delete(retrievedAssignmentConfig);

        retrievedAssignmentConfig = _dao.getAssignmentConfig("lastUpdatedTest");

        assertNull(retrievedAssignmentConfig);
    }

    @Test
    public void deleteAllTest() {
        Date date = new Date();

        AssignmentConfig retrievedAssignmentConfig = _dao.getAssignmentConfig("lastUpdatedTest");

        assertNull(retrievedAssignmentConfig);

        AssignmentConfig assignmentConfig = new AssignmentConfig("lastUpdatedTest", getDateAsString(date));

        _dao.save(assignmentConfig);

        retrievedAssignmentConfig = _dao.getAssignmentConfig("lastUpdatedTest");

        assertNotNull(retrievedAssignmentConfig);

        _dao.deleteAll();

        retrievedAssignmentConfig = _dao.getAssignmentConfig("lastUpdatedTest");

        assertNull(retrievedAssignmentConfig);
    }

    private String getDateAsString(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        return sdf.format(date);
    }
}
