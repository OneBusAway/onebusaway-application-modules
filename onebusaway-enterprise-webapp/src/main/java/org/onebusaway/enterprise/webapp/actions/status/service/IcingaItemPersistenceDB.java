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
