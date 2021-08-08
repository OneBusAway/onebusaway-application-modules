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
import org.onebusaway.admin.model.assignments.Assignment;
import org.onebusaway.admin.service.assignments.AssignmentDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static org.onebusaway.admin.util.DateTimeUtil.*;

import java.util.Date;
import java.util.List;

@Component
public class AssignmentDaoImpl implements AssignmentDao {

    protected static Logger _log = LoggerFactory.getLogger(AssignmentDaoImpl.class);
    private SessionFactory _sessionFactory;

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        _sessionFactory = sessionFactory;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> getAll(Date date) {
        Date from = getStartOfServiceDay(date);
        Date to = getEndOfServiceDay(date);
        Query query = getSession().createQuery("FROM Assignment assign WHERE assign.assignmentId.assignmentDate between :from and :to");
        query.setParameter("from", from);
        query.setParameter("to", to);
        return query.list();
    }

    @Override
    @Transactional(readOnly = true)
    public Assignment getAssignment(String blockId, Date date) {

        Date from = getStartOfServiceDay(date);
        Date to = getEndOfServiceDay(date);
        Query query = getSession().createQuery("FROM Assignment assign WHERE assign.assignmentId.blockId=:blockId AND assign.assignmentId.assignmentDate between :from and :to");
        query.setParameter("blockId", blockId);
        query.setParameter("from", from);
        query.setParameter("to", to);
        return (Assignment) query.uniqueResult();
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void save(Assignment assignment) {
        getSession().saveOrUpdate(assignment);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void saveAll(List<Assignment> assignmentList) {
        for(Assignment assignment : assignmentList){
            save(assignment);
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void delete(Assignment assignment) {
        getSession().delete(assignment);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void deleteAll() {
        Query query = getSession().createQuery("DELETE FROM Assignment");
        query.executeUpdate();
    }

    private Session getSession(){
        return _sessionFactory.getCurrentSession();
    }
}
