/**
 * 
 */
package org.onebusaway.kcmetro2gtfs.handlers;

import org.onebusaway.gtfs.csv.CsvEntityWriter;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.kcmetro.model.TimepointToStopMapping;
import org.onebusaway.kcmetro2gtfs.TranslationContext;
import org.onebusaway.kcmetro2gtfs.calendar.CalendarManager;
import org.onebusaway.kcmetro2gtfs.impl.DirectionIdFactory;
import org.onebusaway.kcmetro2gtfs.impl.MetroDao;
import org.onebusaway.kcmetro2gtfs.model.Indexed;
import org.onebusaway.kcmetro2gtfs.model.MetroKCBlockTrip;
import org.onebusaway.kcmetro2gtfs.model.MetroKCChangeDate;
import org.onebusaway.kcmetro2gtfs.model.MetroKCPatternTimepoint;
import org.onebusaway.kcmetro2gtfs.model.MetroKCServicePattern;
import org.onebusaway.kcmetro2gtfs.model.MetroKCStop;
import org.onebusaway.kcmetro2gtfs.model.MetroKCStopTime;
import org.onebusaway.kcmetro2gtfs.model.MetroKCTrip;
import org.onebusaway.kcmetro2gtfs.model.ServiceId;
import org.onebusaway.kcmetro2gtfs.model.ServiceIdModification;
import org.onebusaway.kcmetro2gtfs.model.ServicePatternKey;
import org.onebusaway.kcmetro2gtfs.model.StopTimepointInterpolation;
import org.onebusaway.kcmetro2gtfs.model.Timepoint;
import org.onebusaway.kcmetro2gtfs.model.TimepointAndIndex;
import org.onebusaway.kcmetro2gtfs.model.TimepointKey;

import edu.washington.cs.rse.collections.CollectionsLibrary;
import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.collections.stats.Min;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.emory.mathcs.backport.java.util.Collections;

public class TripHandler extends EntityHandler<ServicePatternKey, MetroKCTrip> {

  private static String[] TRIPS_FIELDS = {
      "change_date", "trip_id", "db_mod_date", "direction_name", "liftFlag",
      "service_pattern_id", "peakFlag", "scheduleTripId", "schedule_type",
      "exception_code", "ignore=forwardLayover", "ignore=schedTripType",
      "updateDate", "controlPointTime", "ignore=changePrior",
      "ignore=changeNumFollowing", "patternIdFollowing", "patternIdPrior",
      "tripLink"};

  private CsvEntityWriter _writer;

  private MetroDao _dao;

  private CalendarManager _calendarManager;

  private ServicePatternHandler _spHandler;

  private ChangeDateHandler _changeDateHandler;

  private BlockTripHandler _blocks;

  private StopTimeHandler _stopTimeHandler;

  private StopTimeInterpolationHandler _stiHandler;

  private ShapePointHandler _shapeHandler;

  private Map<ServicePatternKey, List<MetroKCTrip>> _tripsByServicePattern;

  private TripNameHandler _nameHandler;

  private TranslationContext _context;

  private Set<Integer> _activeStops = new HashSet<Integer>();

  public TripHandler(TranslationContext context) {
    super(MetroKCTrip.class, TRIPS_FIELDS);

    _context = context;
    _dao = context.getDao();
    _writer = context.getWriter();
    _calendarManager = context.getCalendarManager();

    _spHandler = context.getHandler(ServicePatternHandler.class);
    _changeDateHandler = context.getHandler(ChangeDateHandler.class);
    _blocks = context.getHandler(BlockTripHandler.class);
    _stopTimeHandler = context.getHandler(StopTimeHandler.class);
    _stiHandler = context.getHandler(StopTimeInterpolationHandler.class);
    _shapeHandler = context.getHandler(ShapePointHandler.class);

  }

  @Override
  public void close() {
    super.close();

    _tripsByServicePattern = CollectionsLibrary.mapToValueList(getValues(),
        "servicePattern", ServicePatternKey.class);
  }

  public Set<Integer> getActiveStops() {
    return _activeStops;
  }

  public void writeTrips() {

    System.out.println("writing trips...");

    _nameHandler = _context.getHandler(TripNameHandler.class);

    mergeTripsAndSetChangeDates();

    DirectionIdFactory directionIdFactory = new DirectionIdFactory();

    for (MetroKCTrip trip : getValues()) {

      if (_stopTimeHandler.getTripsToSkip().contains(trip.getId())) {
        _context.addWarning("trip skipped: " + trip.getId());
        continue;
      }

      AgencyAndId metroTripId = new AgencyAndId(_context.getAgencyId(),
          Integer.toString(trip.getId().getId()));

      List<MetroKCChangeDate> changeDates = new ArrayList<MetroKCChangeDate>();
      for (String changeDateId : trip.getChangeDates()) {
        MetroKCChangeDate changeDate = _changeDateHandler.getEntity(changeDateId);
        changeDates.add(changeDate);
      }

      Collections.sort(changeDates);

      ServicePatternKey servicePatternId = trip.getServicePattern();

      Route route = _spHandler.getRouteByServicePatternKey(servicePatternId);

      ServiceId serviceIds = _calendarManager.getServiceIdsForTrip(changeDates,
          trip, route);

      for (ServiceIdModification mod : serviceIds.getModifications()) {

        Trip t = new Trip();
        
        // The trip's agency id should match its route's agency id
        String gtfsTripAgencyId = route.getId().getAgencyId();
        String gtfsTripId = mod.getGtfsTripId(Integer.toString(trip.getId().getId()));
        
        AgencyAndId fullId = new AgencyAndId(gtfsTripAgencyId, gtfsTripId);
        t.setId(fullId);

        t.setRoute(route);

        String headSign = _nameHandler.getNameByTrip(trip);
        t.setTripHeadsign(headSign);

        MetroKCServicePattern p = _dao.getServicePatternByTrip(trip);
        if (p.isExpress())
          t.setRouteShortName(route.getShortName() + "E");

        String serviceId = mod.getServiceId(serviceIds.getServiceId());
        AgencyAndId fullServiceId = new AgencyAndId(_context.getAgencyId(),
            serviceId);
        t.setServiceId(fullServiceId);

        String direction = directionIdFactory.getDirectionId(route.getId(),
            trip);
        t.setDirectionId(direction);

        MetroKCBlockTrip block = _blocks.getBlockForTrip(trip.getId());
        String blockId = Integer.toString(block.getBlockId());
        blockId = mod.getGtfsBlockId(blockId);
        t.setBlockId(blockId);
        t.setBlockSequenceId(block.getTripSequence());

        // The shape id is just the service pattern id
        String shapeId = _shapeHandler.getShapeId(servicePatternId);
        AgencyAndId fullShapeId = new AgencyAndId(_context.getAgencyId(),
            shapeId);
        t.setShapeId(fullShapeId);

        _writer.handleEntity(t);

        double maxShapeDistanceTraveled = 0;

        Map<TimepointAndIndex, MetroKCStopTime> stopTimes = _stopTimeHandler.getStopTimesByTripId(trip.getId());
        SortedMap<Indexed<MetroKCStop>, StopTimepointInterpolation> stis = _stiHandler.getStisByServicePattern(servicePatternId);

        SortedMap<Double, Stop> stopsByDistance = new TreeMap<Double, Stop>();

        for (Map.Entry<Indexed<MetroKCStop>, StopTimepointInterpolation> stiEntry : stis.entrySet()) {

          StopTimepointInterpolation sti = stiEntry.getValue();
          MetroKCStopTime stFrom = stopTimes.get(sti.getTimepointFrom());
          MetroKCStopTime stTo = stopTimes.get(sti.getTimepointTo());

          if (stFrom == null || stTo == null) {
            System.out.println("trip=" + t.getId());
            System.out.println("stopTimes=" + stopTimes);
            System.out.println("sti=" + sti);
            throw new IllegalStateException();
          }

          double t1 = stFrom.getPassingTime();
          double t2 = stTo.getPassingTime();
          int passingTime = (int) ((t1 + (t2 - t1) * sti.getRatio()) * 60);

          passingTime = mod.applyPassingTimeTransformation(passingTime);

          StopTime sto = new StopTime();
          sto.setTrip(t);
          sto.setArrivalTime(passingTime);
          sto.setDepartureTime(passingTime);

          Stop stop = new Stop();
          AgencyAndId stopId = new AgencyAndId(_context.getAgencyId(),
              Integer.toString(sti.getStop()));
          stop.setId(stopId);
          sto.setStop(stop);

          sto.setStopSequence(sti.getStopIndex());
          sto.setShapeDistTraveled(sti.getTotalDistanceTraveled());
          maxShapeDistanceTraveled = Math.max(maxShapeDistanceTraveled,
              sti.getTotalDistanceTraveled());
          _writer.handleEntity(sto);

          stopsByDistance.put(sti.getTotalDistanceTraveled(), stop);

          _activeStops.add(sti.getStop());
        }

        _shapeHandler.setMaxShapeDistanceTraveled(servicePatternId,
            maxShapeDistanceTraveled);

        Map<MetroKCPatternTimepoint, Double> offsets = _stiHandler.getTimepointDistanceOffsetsByServicePattern(servicePatternId);

        for (Map.Entry<MetroKCPatternTimepoint, Double> offsetEntry : offsets.entrySet()) {

          MetroKCPatternTimepoint pt = offsetEntry.getKey();
          
          TimepointAndIndex index = new TimepointAndIndex(pt.getTimepointId(),pt.getSequence());
          MetroKCStopTime stopTime = stopTimes.get(index);
          
          if( stopTime == null)
            throw new IllegalStateException("stop time not found: " + index);

          Min<Stop> min = getClosestStop(stopsByDistance,
              offsetEntry.getValue());

          if (min.isEmpty())
            throw new IllegalStateException("bad");

          Stop stop = min.getMinElement();

          TimepointToStopMapping timepointToStopMapping = new TimepointToStopMapping();

          AgencyAndId timepointId = new AgencyAndId(_context.getAgencyId(),
              Integer.toString(pt.getTimepointId()));

          timepointToStopMapping.setServiceId(fullServiceId);
          timepointToStopMapping.setStopId(stop.getId());
          timepointToStopMapping.setTimepointId(timepointId);
          timepointToStopMapping.setTrackerTripId(metroTripId);
          timepointToStopMapping.setTripId(fullId);
          timepointToStopMapping.setTime((int)(stopTime.getPassingTime() * 60));

          _writer.handleEntity(timepointToStopMapping);

          /**
           * Legacy support for the Timepoint system in the old code-base
           */
          writeTimepoint(trip, gtfsTripId, offsetEntry, pt);
        }
      }
    }
  }

  public long getTripCount(ServicePatternKey id) {
    return getTripsByServicePattern(id).size();
  }

  public List<MetroKCTrip> getTripsByServicePattern(ServicePatternKey id) {
    return _tripsByServicePattern.get(id);
  }

  private Min<Stop> getClosestStop(SortedMap<Double, Stop> stopsByDistance,
      double distance) {

    Min<Stop> min = new Min<Stop>();

    SortedMap<Double, Stop> before = stopsByDistance.headMap(distance);
    SortedMap<Double, Stop> after = stopsByDistance.tailMap(distance);

    if (!before.isEmpty())
      min.add(distance - before.lastKey(), before.get(before.lastKey()));

    if (!after.isEmpty())
      min.add(after.firstKey() - distance, after.get(after.firstKey()));

    return min;
  }

  /**
   * We can potentially have the same trip included in multiple change-dates.
   * KCM staff tell us that this trip should have the same schedule in each
   * changeDate, so our policy is remove all but one of the trips and set each
   * trips changeDates with a call to {@link MetroKCTrip#setChangeDates(Set)}.
   */
  private void mergeTripsAndSetChangeDates() {

    Map<Integer, Set<String>> m = new FactoryMap<Integer, Set<String>>(
        new HashSet<String>());

    for (ServicePatternKey tripId : getKeys())
      m.get(tripId.getId()).add(tripId.getChangeDate());

    for (Map.Entry<Integer, Set<String>> entry : m.entrySet()) {

      Integer tripId = entry.getKey();
      Set<String> changeDates = entry.getValue();

      boolean first = true;

      for (String changeDate : changeDates) {

        ServicePatternKey fullTripId = new ServicePatternKey(changeDate, tripId);
        if (first) {
          MetroKCTrip trip = getEntity(fullTripId);
          trip.setChangeDates(changeDates);
          first = false;
        } else {
          removeEntity(fullTripId);
        }
      }
    }
  }

  @SuppressWarnings("deprecation")
  private void writeTimepoint(MetroKCTrip trip, String gtfsTripId,
      Map.Entry<MetroKCPatternTimepoint, Double> offsetEntry,
      MetroKCPatternTimepoint pt) {
    Timepoint timepoint = new Timepoint();

    TimepointKey key = new TimepointKey();
    key.setGtfsTripId(gtfsTripId);
    key.setTimepointSequence(pt.getSequence());
    key.setTimepointId(Integer.toString(pt.getTimepointId()));
    timepoint.setId(key);
    timepoint.setTrackerTripId(Integer.toString(trip.getId().getId()));
    timepoint.setShapeDistanceTraveled(offsetEntry.getValue());

    _writer.handleEntity(timepoint);
  }

}