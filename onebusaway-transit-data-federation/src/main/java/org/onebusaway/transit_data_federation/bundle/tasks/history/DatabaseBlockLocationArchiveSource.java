package org.onebusaway.transit_data_federation.bundle.tasks.history;

import java.util.List;

import org.hibernate.SessionFactory;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.realtime.history.BlockLocationArchiveRecord;
import org.springframework.orm.hibernate3.HibernateTemplate;

public class DatabaseBlockLocationArchiveSource implements
    BlockLocationArchiveSource {

  private HibernateTemplate _template;

  public void setSessionFactory(SessionFactory sessionFactory) {
    _template = new HibernateTemplate(sessionFactory);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<BlockLocationArchiveRecord> getRecordsForBlock(AgencyAndId blockId) {
    return _template.findByNamedParam(
        "from BlockLocationArchiveRecord where blockId=:blockId", "blockId",
        blockId);
  }
}
