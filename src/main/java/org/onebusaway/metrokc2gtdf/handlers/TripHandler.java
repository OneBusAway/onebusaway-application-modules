/**
 * 
 */
package org.onebusaway.metrokc2gtdf.handlers;

import edu.washington.cs.rse.collections.CollectionsLibrary;

import org.onebusaway.csv.CsvEntityWriter;
import org.onebusaway.gtdf.model.Route;
import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.gtdf.model.StopTime;
import org.onebusaway.gtdf.model.Trip;
import org.onebusaway.metrokc2gtdf.MetroDao;
import org.onebusaway.metrokc2gtdf.TranslationContext;
import org.onebusaway.metrokc2gtdf.calendar.CalendarManager;
import org.onebusaway.metrokc2gtdf.model.Indexed;
import org.onebusaway.metrokc2gtdf.model.MetroKCChangeDate;
import org.onebusaway.metrokc2gtdf.model.MetroKCPatternTimepoint;
import org.onebusaway.metrokc2gtdf.model.MetroKCServicePattern;
import org.onebusaway.metrokc2gtdf.model.MetroKCStop;
import org.onebusaway.metrokc2gtdf.model.MetroKCStopTime;
import org.onebusaway.metrokc2gtdf.model.MetroKCTrip;
import org.onebusaway.metrokc2gtdf.model.ServicePatternKey;
import org.onebusaway.metrokc2gtdf.model.StopTimepointInterpolation;
import org.onebusaway.metrokc2gtdf.model.TimepointAndIndex;
import org.onebusaway.where.model.Timepoint;
import org.onebusaway.where.model.TimepointKey;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class TripHandler extends EntityHandler<Integer, MetroKCTrip> {

  private static String[] TRIPS_FIELDS = {
      "change_date", "id", "db_mod_date", "direction_name", "liftFlag",
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

  private Map<ServicePatternKey, List<MetroKCTrip>> _tripsByServivePattern;

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

    _tripsByServivePattern = CollectionsLibrary.mapToValueList(getValues(),
        "servicePattern", ServicePatternKey.class);
  }

  public void writeTrips() {

    _nameHandler = _context.getHandler(TripNameHandler.class);

    for (MetroKCTrip trip : getValues()) {

      ServicePatternKey servicePatternId = trip.getServicePattern();

      Trip t = new Trip();
      t.setId(trip.getId().toString());

      Route route = _spHandler.getRouteByServicePatternKey(servicePatternId);
      t.setRoute(route);

      String headSign = _nameHandler.getNameByTrip(trip);
      t.setTripHeadsign(headSign);

      MetroKCServicePattern p = _dao.getServicePatternByTrip(trip);
      if (p.isExpress())
        t.setRouteShortName(route.getShortName() + "E");

      MetroKCChangeDate cd = _changeDateHandler.getEntity(servicePatternId.getChangeDate());
      String serviceId = _calendarManager.getServiceIdForTrip(cd, trip);
      t.setServiceId(serviceId);

      t.setDirectionId(trip.getDirectionName());

      int blockId = _blocks.getBlockForTrip(trip.getId());
      t.setBlockId(Integer.toString(blockId));

      // The shape id is just the service pattern id
      String shapeId = _shapeHandler.getShapeId(servicePatternId);
      t.setShapeId(shapeId);

      _writer.handleEntity(t);

      Map<TimepointAndIndex, MetroKCStopTime> stopTimes = _stopTimeHandler.getStopTimesByTripId(trip.getId());
      SortedMap<Indexed<MetroKCStop>, StopTimepointInterpolation> stis = _stiHandler.getStisByServicePattern(servicePatternId);

      for (Map.Entry<Indexed<MetroKCStop>, StopTimepointInterpolation> entry : stis.entrySet()) {

        StopTimepointInterpolation sti = entry.getValue();
        MetroKCStopTime stFrom = stopTimes.get(sti.getTimepointFrom());
        MetroKCStopTime stTo = stopTimes.get(sti.getTimepointTo());

        double t1 = stFrom.getPassingTime();
        double t2 = stTo.getPassingTime();
        int passingTime = (int) ((t1 + (t2 - t1) * sti.getRatio()) * 60);

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

      for (Map.Entry<MetroKCPatternTimepoint, Double> entry : offsets.entrySet()) {

        MetroKCPatternTimepoint pt = entry.getKey();

        Timepoint timepoint = new Timepoint();

        TimepointKey key = new TimepointKey();
        key.setTripId(t.getId());
        key.setTimepointSequence(pt.getSequence());
        key.setTimepointId(Integer.toString(pt.getTimepointId()));
        timepoint.setId(key);

        timepoint.setShapeDistanceTraveled(entry.getValue());

        _writer.handleEntity(timepoint);
      }
    }
  }

  public long getTripCount(ServicePatternKey id) {
    return getTripsByServicePattern(id).size();
  }

  public List<MetroKCTrip> getTripsByServicePattern(ServicePatternKey id) {
    return _tripsByServivePattern.get(id);
  }
}