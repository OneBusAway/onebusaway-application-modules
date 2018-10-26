/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
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
package org.onebusaway.enterprise.webapp.actions.status.service;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.onebusaway.enterprise.webapp.actions.status.model.IcingaItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

@Component
public class IcingaItemPersistenceDB implements IcingaItemPersistence {

    private HibernateTemplate _template;

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        _template = new HibernateTemplate(sessionFactory);
    }

    public HibernateTemplate getHibernateTemplate() {
        return _template;
    }


    @Override
    public List<IcingaItem> getIcingaItems() {
        return _template.execute(new HibernateCallback<List<IcingaItem>>() {
            @SuppressWarnings("unchecked")
            @Override
            public List<IcingaItem> doInHibernate(Session session)
                    throws HibernateException, SQLException {
                Query query = session.createQuery("SELECT icingaItem FROM IcingaItem icingaItem");
                return query.list();
            }
        });
    }
}
