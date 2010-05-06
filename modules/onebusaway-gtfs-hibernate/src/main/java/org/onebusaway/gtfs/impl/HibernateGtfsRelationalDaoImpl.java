package org.onebusaway.gtfs.impl;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.FareAttribute;
import org.onebusaway.gtfs.model.FareRule;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Transfer;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class HibernateGtfsRelationalDaoImpl implements GtfsRelationalDao {

  protected HibernateTemplate _dao;

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    _dao = new HibernateTemplate(sessionFactory);
    _dao.setFlushMode(HibernateTemplate.FLUSH_NEVER);
  }

  public Object execute(HibernateCallback callback) {
    return _dao.execute(callback);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Agency> getAllAgencies() {
    return _dao.find("from Agency");
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<ServiceCalendar> getAllCalendars() {
    return _dao.find("FROM ServiceCalendar");
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<ServiceCalendarDate> getAllCalendarDates() {
    return _dao.find("FROM ServiceCalendarDate");
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<FareAttribute> getAllFareAttributes() {
    return _dao.find("FROM FareAttribute");
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<FareRule> getAllFareRules() {
    return _dao.find("FROM FareRule");
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Frequency> getAllFrequencies() {
    return _dao.find("FROM Frequency");
  }

  @SuppressWarnings("unchecked")
  public List<Route> getAllRoutes() {
    return _dao.find("FROM Route route");
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Stop> getAllStops() {
    return _dao.find("FROM Stop");
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Trip> getAllTrips() {
    return _dao.find("FROM Trip");
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<StopTime> getAllStopTimes() {
    return _dao.find("FROM StopTime");
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<ShapePoint> getAllShapePoints() {
    return _dao.find("FROM ShapePoint");
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Transfer> getAllTransfers() {
    return _dao.find("FROM Transfers");
  }

  @Override
  public Agency getAgencyForId(String id) {
    return (Agency) _dao.get(Agency.class, id);
  }

  @Override
  public FareAttribute getFareAttributeForId(AgencyAndId id) {
    return (FareAttribute) _dao.get(FareAttribute.class, id);
  }

  @Override
  public FareRule getFareRuleForId(int id) {
    return (FareRule) _dao.get(FareRule.class, id);
  }

  @Override
  public Frequency getFrequencyForId(int id) {
    return (Frequency) _dao.get(Frequency.class, id);
  }

  @Override
  public Route getRouteForId(AgencyAndId id) {
    return (Route) _dao.get(Route.class, id);
  }

  @Override
  public ServiceCalendar getCalendarForId(int id) {
    return (ServiceCalendar) _dao.get(ServiceCalendar.class, id);
  }

  @Override
  public ServiceCalendarDate getCalendarDateForId(int id) {
    return (ServiceCalendarDate) _dao.get(ServiceCalendarDate.class, id);
  }

  @Override
  public ShapePoint getShapePointForId(int id) {
    return (ShapePoint) _dao.get(ShapePoint.class, id);
  }

  @Override
  public Stop getStopForId(AgencyAndId agencyAndId) {
    return (Stop) _dao.get(Stop.class, agencyAndId);
  }

  @Override
  public StopTime getStopTimeForId(int id) {
    return (StopTime) _dao.get(StopTime.class, id);
  }

  @Override
  public Transfer getTransferForId(int id) {
    return (Transfer) _dao.get(Transfer.class, id);
  }

  @Override
  public Trip getTripForId(AgencyAndId id) {
    return (Trip) _dao.get(Trip.class, id);
  }

  /****
   * {@link GtfsRelationalDao} Interface
   ****/

  @Override
  public int[] getArrivalTimeIntervalForServiceId(AgencyAndId serviceId) {
    List<?> result = _dao.findByNamedQueryAndNamedParam(
        "arrivalTimeIntervalForServiceId", "serviceId", serviceId);
    return getResultAsIntegerInterval(result);
  }

  @Override
  public int[] getDepartureTimeIntervalForServiceId(AgencyAndId serviceId) {
    List<?> result = _dao.findByNamedQueryAndNamedParam(
        "departureTimeIntervalForServiceId", "serviceId", serviceId);
    return getResultAsIntegerInterval(result);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Route> getRoutesForAgency(Agency agency) {
    return _dao.findByNamedQueryAndNamedParam("routesForAgency", "agency",
        agency);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Trip> getTripsForRoute(Route route) {
    return _dao.findByNamedQueryAndNamedParam("tripsByRoute", "route", route);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<StopTime> getStopTimesForTrip(Trip trip) {
    return _dao.findByNamedQueryAndNamedParam("stopTimesByTrip", "trip", trip);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<ShapePoint> getShapePointsForShapeId(AgencyAndId shapeId) {
    return _dao.findByNamedQueryAndNamedParam("shapePointsForShapeId", "shapeId",
        shapeId);
  }

  /****
   * Private Methods
   ****/

  private int[] getResultAsIntegerInterval(List<?> result) {
    if (result.size() != 1)
      throw new IllegalStateException();

    Object[] row = (Object[]) result.get(0);
    Integer min = (Integer) row[0];
    Integer max = (Integer) row[1];
    if (min == null || max == null)
      return null;
    return new int[] {min, max};
  }
}