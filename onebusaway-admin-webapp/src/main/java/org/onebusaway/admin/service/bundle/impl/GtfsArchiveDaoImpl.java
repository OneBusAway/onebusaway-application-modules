/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.bundle.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.onebusaway.admin.service.bundle.GtfsArchiveDao;
import org.onebusaway.admin.service.bundle.task.model.ArchivedAgency;
import org.onebusaway.admin.service.bundle.task.model.ArchivedCalendar;
import org.onebusaway.admin.service.bundle.task.model.ArchivedRoute;
import org.onebusaway.admin.service.bundle.task.model.ArchivedStopTime;
import org.onebusaway.admin.service.bundle.task.model.ArchivedTrip;
import org.onebusaway.admin.service.bundle.task.model.GtfsBundleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GtfsArchiveDaoImpl implements GtfsArchiveDao {

  protected static Logger _log = LoggerFactory
      .getLogger(GtfsArchiveDaoImpl.class);
  private HibernateTemplate _template;

  @Autowired
  @Qualifier("gtfsRealtimeArchiveSessionFactory")
  public void setSessionFactory(SessionFactory sessionFactory) {
    _template = new HibernateTemplate(sessionFactory);
  }

  /** 
   * Accesses the database to get the all the archived agencies for the 
   * specified bundle id (gid from gtfs_bundle_info).
   * 
   * @return    a List of the ArchivedCalendar entries for this buildId
   */
  @Override
  public List<ArchivedAgency> getAllAgenciesByBundleId(int buildId) {
    Session session = _template.getSessionFactory().getCurrentSession();
    Transaction tx = session.beginTransaction();
    List<ArchivedAgency> archivedAgencies = _template
        .findByNamedParam("from ArchivedAgency where gtfsBundleInfoId=:id", "id", buildId);
    tx.commit();
    return archivedAgencies;
  }

  /** 
   * Accesses the database to get the all the archived calendars for the 
   * specified bundle id (gid from gtfs_bundle_info).
   * 
   * @return    a List of the ArchivedCalendar entries for this buildId
   */
  @Override
  public List<ArchivedCalendar> getAllCalendarsByBundleId(int buildId) {
    Session session = _template.getSessionFactory().getCurrentSession();
    Transaction tx = session.beginTransaction();
    List<ArchivedCalendar> archivedCalendars = _template
        .findByNamedParam("from ArchivedCalendar where gtfsBundleInfoId=:id", "id", buildId);
//    List<ArchivedCalendar> archivedCalendars = session.createCriteria(ArchivedCalendar.class).list();
    tx.commit();
    //GtfsBundleInfo bundleInfo = responses.get(0);
    return archivedCalendars;
  }
  
  @Override
  public List<ArchivedRoute> getAllRoutesByBundleId(int buildId) {
    Session session = _template.getSessionFactory().getCurrentSession();
    Transaction tx = session.beginTransaction();
    List<ArchivedRoute> archivedRoutes = _template
        .findByNamedParam("from ArchivedRoute where gtfsBundleInfoId=:id", "id", buildId);
    tx.commit();
    return archivedRoutes;
  }

  @Override
  public List<ArchivedTrip> getAllTripsByBundleId(int buildId) {
    Session session = _template.getSessionFactory().getCurrentSession();
    Transaction tx = session.beginTransaction();
    List<ArchivedTrip> archivedTrips = _template
        .findByNamedParam("from ArchivedTrip where gtfsBundleInfoId=:id order by route_agencyId, route_id", "id", buildId);
    tx.commit();
    return archivedTrips;
  }

  /** 
   * Accesses the database to get the names of all the archived datasets.
   * 
   * @return    a SortedSet of the dataset names.
   */
  @Override
  public SortedSet<String> getAllDatasets() {
    SortedSet<String> datasets = new TreeSet<>();
    
    Session session = _template.getSessionFactory().getCurrentSession();
    Transaction tx = session.beginTransaction();
    List<GtfsBundleInfo> archivedBundles = session.createCriteria(GtfsBundleInfo.class).list();
    tx.commit();
    for (GtfsBundleInfo archivedBundle : archivedBundles) {
      datasets.add(archivedBundle.getDirectory());
    }
    return datasets;
  }

  /** 
   * Accesses the database to get the list of build names for the 
   * specified dataset..
   * 
   * @return    a SortedSet of the dataset names.
   */
  @Override
  public SortedSet<String> getBuildNamesForDataset(String dataset) {
    SortedSet<String> buildNames = new TreeSet<>();
    Session session = _template.getSessionFactory().getCurrentSession();
    Transaction tx = session.beginTransaction();
    List<GtfsBundleInfo> archivedBundles = session.createCriteria(GtfsBundleInfo.class).list();
    tx.commit();
    for (GtfsBundleInfo archivedBundle : archivedBundles) {
      if (archivedBundle.getDirectory().equals(dataset)) {
        buildNames.add(archivedBundle.getName());
      }
    }
    return buildNames;
  }

  @Override
  public SortedMap<String, String> getBuildNameMapForDataset(String dataset) {
    SortedMap<String, String> buildNameMap = new TreeMap<>();
    Session session = _template.getSessionFactory().getCurrentSession();
    Transaction tx = session.beginTransaction();
    List<GtfsBundleInfo> archivedBundles = session.createCriteria(GtfsBundleInfo.class).list();
    tx.commit();
    for (GtfsBundleInfo archivedBundle : archivedBundles) {
      if (archivedBundle.getDirectory().equals(dataset)) {
        buildNameMap.put(archivedBundle.getName(), ""+archivedBundle.getId());
      }
    }
    return buildNameMap;
    
  }

  @Override
  public GtfsBundleInfo getBundleInfoForId(int buildId) {
    Session session = _template.getSessionFactory().getCurrentSession();
    Transaction tx = session.beginTransaction();
    List<GtfsBundleInfo> responses = _template
        .findByNamedParam("from GtfsBundleInfo where gid=:id", "id", buildId);
    tx.commit();
    GtfsBundleInfo bundleInfo = responses.get(0);
    return bundleInfo;
  }

  @Override
  public List<ArchivedRoute> getRoutesForAgencyAndBundleId(
      String agencyId, int buildId) {
    Session session = _template.getSessionFactory().getCurrentSession();
    Transaction tx = session.beginTransaction();
    Query query = session.createQuery("from ArchivedRoute where gtfs_bundle_info_id=:build_id and agencyId=:agencyId")
        .setParameter("build_id", buildId)
        .setParameter("agencyId", agencyId);
    List<ArchivedRoute> archivedRoutes = query.list();
    
    tx.commit();
    return archivedRoutes;
  }

  @Override
  public List<ArchivedStopTime> getStopTimesForTripAndBundleId(
      ArchivedTrip trip, int buildId) {
    String tripAgencyId = trip.getAgencyId();
    String tripId = trip.getId();
    Session session = _template.getSessionFactory().getCurrentSession();
    Transaction tx = session.beginTransaction();
    Query query = session.createQuery("from ArchivedStopTime where gtfs_bundle_info_id=:build_id and trip_agencyId=:tripAgencyId "
        + "and trip_id=:tripId")
        .setParameter("build_id", buildId)
        .setParameter("tripAgencyId", tripAgencyId)
        .setParameter("tripId", tripId);
    List<ArchivedStopTime> archivedStopTimes = query.list();
    
    tx.commit();
    return archivedStopTimes;
  }

  @Override
  public List<ArchivedTrip> getTripsForRouteAndBundleId(String routeAgencyAndId,
      int buildId) {
    String routeAgencyId = routeAgencyAndId.split("_")[0];
    String routeId = routeAgencyAndId.split("_")[1];
    Session session = _template.getSessionFactory().getCurrentSession();
    Transaction tx = session.beginTransaction();
    Query query = session.createQuery("from ArchivedTrip where gtfs_bundle_info_id=:build_id and route_agencyId=:routeAgencyId "
        + "and route_id=:routeId")
        .setParameter("build_id", buildId)
        .setParameter("routeAgencyId", routeAgencyId)
        .setParameter("routeId", routeId);
    List<ArchivedTrip> archivedTrips = query.list();
    
    tx.commit();
    return archivedTrips;
  }

  @Override
  public List getTripStopCounts(int buildId) {
    Session session = _template.getSessionFactory().getCurrentSession();
    Transaction tx = session.beginTransaction();
    Query query = session.getNamedQuery("tripStopCts")
        .setParameter("bundleId", buildId);
    List results = query.list();

    tx.commit();
    return results;
  }

}
