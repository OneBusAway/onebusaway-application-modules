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
package org.onebusaway.transit_data_federation.impl.reporting;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.onebusaway.collections.tuple.T2;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.problems.EProblemReportStatus;
import org.onebusaway.transit_data_federation.services.reporting.UserReportingDao;
import org.springframework.beans.factory.annotation.Autowired;
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
  public List<T2<AgencyAndId, Integer>> getTripProblemReportSummaries(
      String agencyId, long timeFrom, long timeTo, EProblemReportStatus status) {

    List<Object[]> records = null;

    if (status == null) {
      String[] names = {"agencyId", "timeFrom", "timeTo"};
      Object[] values = {agencyId, timeFrom, timeTo};
      records = _template.findByNamedQueryAndNamedParam(
          "tripProblemReportSummaries", names, values);
    } else {
      String[] names = {"agencyId", "timeFrom", "timeTo", "status"};
      Object[] values = {agencyId, timeFrom, timeTo, status};
      records = _template.findByNamedQueryAndNamedParam(
          "tripProblemReportSummariesWithStatus", names, values);
    }

    List<T2<AgencyAndId, Integer>> results = new ArrayList<T2<AgencyAndId, Integer>>(
        records.size());

    for (Object[] record : records) {
      AgencyAndId tripId = (AgencyAndId) record[0];
      Long count = (Long) record[1];
      results.add(Tuples.tuple(tripId, count.intValue()));
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
  public List<TripProblemReportRecord> getTripProblemReports(String agencyId,
      long timeFrom, long timeTo, EProblemReportStatus status) {

    if (status == null) {
      String[] names = {"agencyId", "timeFrom", "timeTo"};
      Object[] values = {agencyId, timeFrom, timeTo};
      return _template.findByNamedQueryAndNamedParam("tripProblemReports",
          names, values);
    } else {
      String[] names = {"agencyId", "timeFrom", "timeTo", "status"};
      Object[] values = {agencyId, timeFrom, timeTo, status};
      return _template.findByNamedQueryAndNamedParam(
          "tripProblemReportsWithStatus", names, values);
    }
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
}
