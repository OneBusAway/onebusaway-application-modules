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

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.onebusaway.admin.model.assignments.AssignmentConfig;
import org.onebusaway.admin.service.assignments.AssignmentConfigDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class AssignmentConfigDaoImpl implements AssignmentConfigDao {

    protected static Logger _log = LoggerFactory.getLogger(AssignmentConfigDaoImpl.class);
    private SessionFactory _sessionFactory;

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        _sessionFactory = sessionFactory;
    }

    @Override
    public List<AssignmentConfig> getAll() {
        return getSession().createCriteria(AssignmentConfig.class).list();
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentConfig getAssignmentConfig(String key) {
        return (AssignmentConfig) getSession().get(AssignmentConfig.class, key);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void save(AssignmentConfig assignmentConfig) {
        getSession().saveOrUpdate(assignmentConfig);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void saveAll(List<AssignmentConfig> assignmentConfigList) {
        for(AssignmentConfig assignmentConfig : assignmentConfigList){
            getSession().saveOrUpdate(assignmentConfig);
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void delete(AssignmentConfig assignmentConfig) {
        getSession().delete(assignmentConfig);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void deleteAll() {
        Query query = getSession().createQuery("DELETE from AssignmentConfig");
        query.executeUpdate();
    }

    private Session getSession(){
        return _sessionFactory.getCurrentSession();
    }
}
