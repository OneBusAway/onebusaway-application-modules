/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.transit_data_federation.impl.reporting;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.onebusaway.collections.tuple.T2;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.problems.EProblemReportStatus;
import org.onebusaway.transit_data.model.problems.ETripProblemGroupBy;
import org.onebusaway.transit_data.model.problems.TripProblemReportQueryBean;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.reporting.UserReportingDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

@Component
class UserReportingDaoImpl implements UserReportingDao {

  private HibernateTemplate _template;

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    _template = new HibernateTemplate(sessionFactory);
  }

  @Override
  public void saveOrUpdate(Object record) {
    _template.saveOrUpdate(record);
  }

  @Override
  public void delete(Object entity) {
    _template.delete(entity);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<T2<AgencyAndId, Integer>> getStopProblemReportSummaries(
      String agencyId, long timeFrom, long timeTo, EProblemReportStatus status) {

    List<Object[]> records = null;

    if (status == null) {
      String[] names = {"agencyId", "timeFrom", "timeTo"};
      Object[] values = {agencyId, timeFrom, timeTo};
      records = _template.findByNamedQueryAndNamedParam(
          "stopProblemReportSummaries", names, values);
    } else {
      String[] names = {"agencyId", "timeFrom", "timeTo", "status"};
      Object[] values = {agencyId, timeFrom, timeTo, status};
      records = _template.findByNamedQueryAndNamedParam(
          "stopProblemReportSummariesWithStatus", names, values);
    }

    List<T2<AgencyAndId, Integer>> results = new ArrayList<T2<AgencyAndId, Integer>>(
        records.size());

    for (Object[] record : records) {
      AgencyAndId stopId = (AgencyAndId) record[0];
      Long count = (Long) record[1];
      results.add(Tuples.tuple(stopId, count.intValue()));
    }

    return results;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<T2<Object, Integer>> getTripProblemReportSummaries(
      final TripProblemReportQueryBean query, final ETripProblemGroupBy groupBy) {

    List<Object[]> rows = _template.executeFind(new HibernateCallback<List<Object[]>>() {

      @Override
      public List<Object[]> doInHibernate(Session session)
          throws HibernateException, SQLException {

        Criteria c = session.createCriteria(TripProblemReportRecord.class);

        ProjectionList projections = Projections.projectionList();
        projections.add(Projections.rowCount());
        switch (groupBy) {
          case TRIP:
            projections.add(Projections.groupProperty("tripId.agencyId"));
            projections.add(Projections.groupProperty("tripId.id"));
            break;
          case STATUS:
            projections.add(Projections.groupProperty("status"));
            break;
          case LABEL:
            projections.add(Projections.groupProperty("label"));
            break;
        }
        c.setProjection(projections);

        addQueryToCriteria(query, c);

        return c.list();
      }
    });

    List<T2<Object, Integer>> results = new ArrayList<T2<Object, Integer>>(
        rows.size());

    for (Object[] row : rows) {
      Integer count = (Integer) row[0];
      Object key = getKeyForTripProblemReportSummariesRow(row, groupBy);
      results.add(Tuples.tuple(key, count.intValue()));
    }

    return results;
  }

  @SuppressWarnings("unchecked")
  public List<StopProblemReportRecord> getStopProblemReports(String agencyId,
      long timeFrom, long timeTo, EProblemReportStatus status) {

    if (status == null) {
      String[] names = {"agencyId", "timeFrom", "timeTo"};
      Object[] values = {agencyId, timeFrom, timeTo};
      return _template.findByNamedQueryAndNamedParam("stopProblemReports",
          names, values);
    } else {
      String[] names = {"agencyId", "timeFrom", "timeTo", "status"};
      Object[] values = {agencyId, timeFrom, timeTo, status};
      return _template.findByNamedQueryAndNamedParam(
          "stopProblemReportsWithStatus", names, values);
    }
  }

  @SuppressWarnings("unchecked")
  public List<TripProblemReportRecord> getTripProblemReports(
      final TripProblemReportQueryBean query) {

    return _template.executeFind(new HibernateCallback<List<TripProblemReportRecord>>() {

      @Override
      public List<TripProblemReportRecord> doInHibernate(Session session)
          throws HibernateException, SQLException {

        Criteria c = session.createCriteria(TripProblemReportRecord.class);
        addQueryToCriteria(query, c);
        c.addOrder(Order.asc("time"));
        return c.list();
      }

    });
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<StopProblemReportRecord> getAllStopProblemReportsForStopId(
      AgencyAndId stopId) {
    return _template.findByNamedQueryAndNamedParam(
        "allStopProblemReportsForStopId", "stopId", stopId);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<TripProblemReportRecord> getAllTripProblemReportsForTripId(
      AgencyAndId tripId) {
    return _template.findByNamedQueryAndNamedParam(
        "allTripProblemReportsForTripId", "tripId", tripId);
  }

  @Override
  public StopProblemReportRecord getStopProblemRecordForId(long id) {
    return _template.get(StopProblemReportRecord.class, id);
  }

  @Override
  public TripProblemReportRecord getTripProblemRecordForId(long id) {
    return _template.get(TripProblemReportRecord.class, id);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<String> getAllTripProblemReportLabels() {
    return _template.findByNamedQuery("allTripProblemReportLabels");
  }

  private Object getKeyForTripProblemReportSummariesRow(Object[] row,
      ETripProblemGroupBy groupBy) {
    switch (groupBy) {
      case TRIP:
        return new AgencyAndId((String) row[1], (String) row[2]);
      case STATUS:
        return row[1];
      case LABEL:
        return row[1];
    }
    throw new IllegalStateException("unhandled grouping: " + groupBy);
  }

  private void addQueryToCriteria(TripProblemReportQueryBean query, Criteria c) {
    if (query.getAgencyId() != null) {
      c.add(Property.forName("tripId.agencyId").eq(query.getAgencyId()));
    }
    if (query.getTripId() != null) {
      c.add(Property.forName("tripId").eq(
          AgencyAndIdLibrary.convertFromString(query.getTripId())));
    }
    if (query.getTimeFrom() != 0) {
      c.add(Property.forName("time").ge(query.getTimeFrom()));
    }
    if (query.getTimeTo() != 0) {
      c.add(Property.forName("time").le(query.getTimeTo()));
    }
    if (query.getStatus() != null) {
      c.add(Property.forName("status").eq(query.getStatus()));
    }
    if (query.getLabel() != null) {
      c.add(Property.forName("label").eq(query.getLabel()));
    }
  }
}
