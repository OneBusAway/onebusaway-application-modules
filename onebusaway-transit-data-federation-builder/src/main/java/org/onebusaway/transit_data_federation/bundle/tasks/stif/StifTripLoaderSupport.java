/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.transit_data_federation.bundle.tasks.stif;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.onebusaway.gtfs.impl.SpringHibernateGtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.HibernateOperation;
import org.onebusaway.transit_data_federation.bundle.tasks.stif.model.ServiceCode;
import org.onebusaway.transit_data_federation.bundle.tasks.stif.model.TripRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a mapping from Destination Sign Code (DSC) to GTFS Trip objects using
 * data in STIF, MTA's internal format.
 */
public class StifTripLoaderSupport {

  private static final Logger _log = LoggerFactory.getLogger(StifTripLoaderSupport.class);

  private GtfsMutableRelationalDao gtfsDao;

  private Map<TripIdentifier, List<Trip>> tripsByIdentifier;

  private Map<String, String> stopIdsByLocation = new HashMap<String, String>();

  private int _totalTripCount;

  public void setGtfsDao(GtfsMutableRelationalDao dao) {
    this.gtfsDao = dao;
  }

  public static ServiceCode scheduleIdForGtfsDayCode(String dayCode) {
    return ServiceCode.getServiceCodeForId(dayCode);
  }

  public int getTotalTripCount() {
    return _totalTripCount;
  }

  public void putStopIdForLocation(String location, String stopId) {
    stopIdsByLocation.put(location, stopId);
  }

  public TripIdentifier getIdentifierForStifTrip(TripRecord tripRecord, StifTrip rawTrip) {
    String routeName = tripRecord.getSignCodeRoute();
    if (routeName == null || routeName.trim().length() == 0) {
      routeName = tripRecord.getRunRoute();
      routeName = routeName.replaceFirst("^([a-zA-Z]+)0+", "$1").toUpperCase();
    }
    String startStop = rawTrip.firstStop;
    int startTime = rawTrip.firstStopTime;
    int endTime = rawTrip.lastStopTime;
    if (startTime < 0) {
      // skip a day ahead for previous-day trips.
      startTime += 24 * 60 * 60;
      endTime += 24 * 60 * 60;
    }
    String run = tripRecord.getRunId();
    return new TripIdentifier(routeName, startTime, endTime, startStop, run, rawTrip.blockId);
  }

  public List<Trip> getTripsForIdentifier(TripIdentifier id) {

    /**
     * Lazy initialization
     */
    if (tripsByIdentifier == null) {

      tripsByIdentifier = new HashMap<TripIdentifier, List<Trip>>();

      Collection<Trip> allTrips = gtfsDao.getAllTrips();
      _totalTripCount = allTrips.size();
      int index = 0;

      for (Trip trip : allTrips) {

        if (index % 20000 == 0)
          _log.info("trip=" + index + " / " + allTrips.size());
        index++;

        TripIdentifier tripIdentifier = getTripAsIdentifier(trip);
        List<Trip> trips = tripsByIdentifier.get(tripIdentifier);
        if (trips == null) {
          trips = new ArrayList<Trip>();
          tripsByIdentifier.put(tripIdentifier, trips);
        }
        trips.add(trip);
      }
    }

    return tripsByIdentifier.get(id);
  }

  /****
   * Private Methods
   ****/

  public String getStopIdForLocation(String originLocation) {
    return stopIdsByLocation.get(originLocation);
  }

  TripIdentifier getTripAsIdentifier(final Trip trip) {

    String routeName = trip.getRoute().getId().getId();
    int startTime = -1, endTime = -1;
    String startStop;

    /**
     * This is WAY faster if we are working in hibernate, in that we don't need
     * to look up EVERY StopTime for a Trip, along with the Stop objects for
     * those StopTimes. Instead, we just grab the first departure and last
     * arrival times.
     */
    if (gtfsDao instanceof SpringHibernateGtfsRelationalDaoImpl) {
      SpringHibernateGtfsRelationalDaoImpl dao = (SpringHibernateGtfsRelationalDaoImpl) gtfsDao;
      List<?> rows = (List<?>) dao.execute(new HibernateOperation() {
        @Override
        public Object doInHibernate(Session session) throws HibernateException,
            SQLException {
          Query query = session.createQuery("SELECT st.departureTime, st.stop.id.id FROM StopTime st WHERE st.trip = :trip AND st.departureTime >= 0 ORDER BY st.stopSequence ASC LIMIT 1");
          query.setParameter("trip", trip);
          return query.list();
        }
      });
      Object[] values = (Object[]) rows.get(0);
      startTime = ((Integer) values[0]);
      startStop = (String) values[1];

      rows = (List<?>) dao.execute(new HibernateOperation() {
        @Override
        public Object doInHibernate(Session session) throws HibernateException,
            SQLException {
          Query query = session.createQuery("SELECT st.arrivalTime FROM StopTime st WHERE st.trip = :trip AND st.arrivalTime >= 0 ORDER BY st.stopSequence DESC LIMIT 1");
          query.setParameter("trip", trip);
          return query.list();
        }
      });
      values = (Object[]) rows.get(0);
      endTime = ((Integer) values[0]);
    } else {
      List<StopTime> stopTimes = gtfsDao.getStopTimesForTrip(trip);
      StopTime startStopTime = stopTimes.get(0);
      startTime = startStopTime.getDepartureTime();
      startStop = startStopTime.getStop().getId().getId();
      StopTime endStopTime = stopTimes.get(stopTimes.size() - 1);
      endTime = endStopTime.getArrivalTime();
    }
    String run = null;
    String[] parts = trip.getId().getId().toUpperCase().split("_");
    if (parts.length > 2) {
      //hack the run out of the trip id.  This depends sensitively on the MTA maintaining
      //their current trip id format.
      //for MTA Bus Co, this is not necessary, we hope
      //also works for new NYCT GTFS format.
      String runRoute = parts[parts.length-2];
      String runNumber = parts[parts.length-1];
      run = runRoute + "-" + runNumber;
    }
    routeName = routeName.replaceFirst("^([a-zA-Z]+)0+", "$1").toUpperCase();
    return new TripIdentifier(routeName, startTime, endTime, startStop, run, trip.getBlockId());
  }

  public GtfsMutableRelationalDao getGtfsDao() {
    return gtfsDao;
  }

}
