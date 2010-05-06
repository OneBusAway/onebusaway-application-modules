/**
 * 
 */
package org.onebusaway.metrokc2gtfs.handlers;

import org.onebusaway.csv.CsvEntityWriter;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.metrokc2gtfs.TranslationContext;
import org.onebusaway.metrokc2gtfs.calendar.CalendarManager;
import org.onebusaway.metrokc2gtfs.impl.MetroDao;
import org.onebusaway.metrokc2gtfs.model.Indexed;
import org.onebusaway.metrokc2gtfs.model.MetroKCBlockTrip;
import org.onebusaway.metrokc2gtfs.model.MetroKCChangeDate;
import org.onebusaway.metrokc2gtfs.model.MetroKCPatternTimepoint;
import org.onebusaway.metrokc2gtfs.model.MetroKCServicePattern;
import org.onebusaway.metrokc2gtfs.model.MetroKCStop;
import org.onebusaway.metrokc2gtfs.model.MetroKCStopTime;
import org.onebusaway.metrokc2gtfs.model.MetroKCTrip;
import org.onebusaway.metrokc2gtfs.model.ServiceId;
import org.onebusaway.metrokc2gtfs.model.ServiceIdModification;
import org.onebusaway.metrokc2gtfs.model.ServicePatternKey;
import org.onebusaway.metrokc2gtfs.model.StopTimepointInterpolation;
import org.onebusaway.metrokc2gtfs.model.TimepointAndIndex;
import org.onebusaway.where.model.Timepoint;
import org.onebusaway.where.model.TimepointKey;

import edu.washington.cs.rse.collections.CollectionsLibrary;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class TripHandler extends EntityHandler<Integer, MetroKCTrip> {

  private static String[] TRIPS_FIELDS = {
      "change_date", "id", "db_mod_date", "direction_name", "liftFlag", "service_pattern_id", "peakFlag",
      "scheduleTripId", "schedule_type", "exception_code", "ignore=forwardLayover", "ignore=schedTripType",
      "updateDate", "controlPointTime", "ignore=changePrior", "ignore=changeNumFollowing", "patternIdFollowing",
      "patternIdPrior", "tripLink"};

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

    _tripsByServicePattern = CollectionsLibrary.mapToValueList(getValues(), "servicePattern", ServicePatternKey.class);
  }

  public void writeTrips() {

    _nameHandler = _context.getHandler(TripNameHandler.class);

    for (MetroKCTrip trip : getValues()) {

      ServicePatternKey servicePatternId = trip.getServicePattern();
      MetroKCChangeDate cd = _changeDateHandler.getEntity(servicePatternId.getChangeDate());
      Route route = _spHandler.getRouteByServicePatternKey(servicePatternId);

      ServiceId serviceIds = _calendarManager.getServiceIdsForTrip(cd, trip, route);

      for (ServiceIdModification mod : serviceIds.getModifications()) {

        Trip t = new Trip();
        String gtfsTripId = mod.getGtfsTripId(trip.getId().toString());
        t.setId(gtfsTripId);

        t.setRoute(route);

        String headSign = _nameHandler.getNameByTrip(trip);
        t.setTripHeadsign(headSign);

        MetroKCServicePattern p = _dao.getServicePatternByTrip(trip);
        if (p.isExpress())
          t.setRouteShortName(route.getShortName() + "E");

        t.setServiceId(mod.getServiceId(serviceIds.getServiceId()));

        t.setDirectionId(trip.getDirectionName());

        MetroKCBlockTrip block = _blocks.getBlockForTrip(trip.getId());
        String blockId = Integer.toString(block.getBlockId());
        blockId = mod.getGtfsBlockId(blockId);
        t.setBlockId(blockId);
        t.setBlockSequenceId(block.getTripSequence());

        // The shape id is just the service pattern id
        String shapeId = _shapeHandler.getShapeId(servicePatternId);
        t.setShapeId(shapeId);

        _writer.handleEntity(t);

        Map<TimepointAndIndex, MetroKCStopTime> stopTimes = _stopTimeHandler.getStopTimesByTripId(trip.getId());
        SortedMap<Indexed<MetroKCStop>, StopTimepointInterpolation> stis = _stiHandler.getStisByServicePattern(servicePatternId);

        for (Map.Entry<Indexed<MetroKCStop>, StopTimepointInterpolation> stiEntry : stis.entrySet()) {

          StopTimepointInterpolation sti = stiEntry.getValue();
          MetroKCStopTime stFrom = stopTimes.get(sti.getTimepointFrom());
          MetroKCStopTime stTo = stopTimes.get(sti.getTimepointTo());
          
          if( stFrom == null || stTo == null) {
            System.out.println("trip=" + t.getId());
            System.out.println("stopTimes=" + stopTimes);
            System.out.println("sti=" + sti);            
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
          stop.setId(Integer.toString(sti.getStop()));
          sto.setStop(stop);
          sto.setStopSequence(sti.getStopIndex());
          sto.setShapeDistanceTraveled(sti.getTotalDistanceTraveled());

          _writer.handleEntity(sto);
        }

        Map<MetroKCPatternTimepoint, Double> offsets = _stiHandler.getTimepointDistanceOffsetsByServicePattern(servicePatternId);

        for (Map.Entry<MetroKCPatternTimepoint, Double> offsetEntry : offsets.entrySet()) {

          MetroKCPatternTimepoint pt = offsetEntry.getKey();

          Timepoint timepoint = new Timepoint();

          TimepointKey key = new TimepointKey();
          key.setTripId(t.getId());
          key.setTimepointSequence(pt.getSequence());
          key.setTimepointId(Integer.toString(pt.getTimepointId()));
          timepoint.setId(key);
          timepoint.setGtfsTripId(gtfsTripId);
          timepoint.setShapeDistanceTraveled(offsetEntry.getValue());

          _writer.handleEntity(timepoint);
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
}