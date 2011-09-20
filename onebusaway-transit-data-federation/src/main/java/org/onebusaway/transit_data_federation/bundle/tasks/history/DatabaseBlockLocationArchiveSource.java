/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
  public List<BlockLocationArchiveRecord> getRecordsForTrip(AgencyAndId tripId) {
    return _template.findByNamedParam(
        "from BlockLocationArchiveRecord where tripId=:tripId", "tripId",
        tripId);
  }
}
