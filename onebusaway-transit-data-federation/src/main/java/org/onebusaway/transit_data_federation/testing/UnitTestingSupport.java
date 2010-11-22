package org.onebusaway.transit_data_federation.testing;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockIndicesFactory;
import org.onebusaway.transit_data_federation.bundle.tasks.transit_graph.BlockConfigurationEntriesFactory;
import org.onebusaway.transit_data_federation.bundle.tasks.transit_graph.ServiceIdOverlapCache;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockConfigurationEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockConfigurationEntryImpl.Builder;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockStopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockTripEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.FrequencyEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.StopTransferList;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripIndex;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;

public class UnitTestingSupport {

  private static final DateFormat _format = new SimpleDateFormat(
      "yyyy-MM-dd HH:mm");

  private static final TimeZone _timeZone = TimeZone.getTimeZone("America/Los_Angeles");

  static {
    _format.setTimeZone(_timeZone);
  }

  /****
   * Time and Date Methods
   ****/

  public static TimeZone timeZone() {
    return _timeZone;
  }

  /**
   * @param source format is "yyyy-MM-dd HH:mm"
   * @return
   */
  public static Date date(String source) {
    try {
      return _format.parse(source);
    } catch (ParseException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public static long dateAsLong(String source) {
    return UnitTestingSupport.date(source).getTime();
  }

  public static String format(Date dateA) {
    return UnitTestingSupport._format.format(dateA);
  }

  public static Date getTimeAsDay(Date t) {
    return getTimeAsDay(t.getTime());
  }

  public static Date getTimeAsDay(long t) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeZone(timeZone());
    cal.setTimeInMillis(t);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTime();
  }

  public static final int hourToSec(double hour) {
    return (int) (hour * 60 * 60);
  }

  public static int time(int hour, int minute, int seconds) {
    return (hour * 60 + minute) * 60 + seconds;
  }

  public static int time(int hour, int minute) {
    return time(hour, minute, 0);
  }

  /****
   * Entity Factory Methods
   ****/

  public static StopEntryImpl stop(String id, double lat, double lon) {
    return new StopEntryImpl(aid(id), lat, lon);
  }

  public static void addTransfer(StopEntryImpl from, StopEntryImpl to) {

    double distance = SphericalGeometryLibrary.distance(from.getStopLocation(),
        to.getStopLocation());
    StopTransfer transfer = new StopTransfer(to, 0, distance);

    List<StopTransfer> transfers = new ArrayList<StopTransfer>();
    StopTransferList existing = from.getTransfers();
    if (existing != null)
      transfers.addAll(existing);
    transfers.add(transfer);
    existing = new StopTransferList(transfers);
    from.setTransfers(existing);
  }

  public static BlockEntryImpl block(String id) {
    BlockEntryImpl block = new BlockEntryImpl();
    block.setId(aid(id));
    return block;
  }

  public static TripEntryImpl trip(String id) {
    TripEntryImpl trip = new TripEntryImpl();
    trip.setId(aid(id));
    return trip;
  }

  public static TripEntryImpl trip(String id, String serviceId) {
    TripEntryImpl trip = trip(id);
    trip.setServiceId(new LocalizedServiceId(aid(serviceId), timeZone()));
    return trip;
  }

  public static TripEntryImpl trip(String id, String serviceId,
      double totalTripDistance) {
    TripEntryImpl trip = trip(id, serviceId);
    trip.setTotalTripDistance(totalTripDistance);
    return trip;
  }

  public static TripEntryImpl trip(String id, double totalTripDistance) {
    TripEntryImpl trip = trip(id);
    trip.setTotalTripDistance(totalTripDistance);
    return trip;
  }

  public static FrequencyEntry frequency(int startTime, int endTime,
      int headwaySecs) {
    return new FrequencyEntryImpl(startTime, endTime, headwaySecs);
  }

  public static BlockConfigurationEntry linkBlockTrips(BlockEntryImpl block,
      TripEntryImpl... trips) {
    return linkBlockTrips(block, null, trips);
  }

  public static BlockConfigurationEntry linkBlockTrips(BlockEntryImpl block,
      List<FrequencyEntry> frequencies, TripEntryImpl... trips) {

    List<TripEntry> tripEntries = new ArrayList<TripEntry>();
    Set<LocalizedServiceId> serviceIds = new TreeSet<LocalizedServiceId>();
    for (int i = 0; i < trips.length; i++) {
      TripEntryImpl trip = trips[i];
      trip.setBlock(block);
      tripEntries.add(trip);
      if (trip.getServiceId() != null)
        serviceIds.add(trip.getServiceId());
    }
    Builder builder = BlockConfigurationEntryImpl.builder();
    builder.setBlock(block);
    builder.setServiceIds(new ServiceIdActivation(
        new ArrayList<LocalizedServiceId>(serviceIds),
        new ArrayList<LocalizedServiceId>()));
    builder.setTrips(tripEntries);
    builder.setFrequencies(frequencies);
    builder.setTripGapDistances(new double[tripEntries.size()]);

    BlockConfigurationEntry configuration = builder.create();

    List<BlockConfigurationEntry> configurations = block.getConfigurations();
    if (configurations == null) {
      configurations = new ArrayList<BlockConfigurationEntry>();
      block.setConfigurations(configurations);
    }
    configurations.add(configuration);

    return configuration;
  }

  public static void linkBlockTrips(CalendarService calendarService,
      BlockEntryImpl block, TripEntryImpl... trips) {

    ServiceIdOverlapCache cache = new ServiceIdOverlapCache();
    cache.setCalendarService(calendarService);

    BlockConfigurationEntriesFactory factory = new BlockConfigurationEntriesFactory();
    factory.setServiceIdOverlapCache(cache);

    List<TripEntryImpl> tripsInBlock = new ArrayList<TripEntryImpl>();
    for (TripEntryImpl trip : trips)
      tripsInBlock.add(trip);

    factory.processBlockConfigurations(block, tripsInBlock);
  }

  public static BlockConfigurationEntry linkBlockTrips(String blockId,
      TripEntryImpl... trips) {
    return linkBlockTrips(block(blockId), trips);
  }

  public static BlockConfigurationEntry findBlockConfig(BlockEntry blockEntry,
      ServiceIdActivation serviceIds) {
    for (BlockConfigurationEntry blockConfig : blockEntry.getConfigurations()) {
      if (blockConfig.getServiceIds().equals(serviceIds))
        return blockConfig;
    }
    return null;
  }

  public static List<BlockTripIndex> blockTripIndices(BlockEntryImpl... blocks) {
    List<BlockEntry> list = new ArrayList<BlockEntry>();
    for (BlockEntryImpl block : blocks)
      list.add(block);
    BlockIndicesFactory factory = new BlockIndicesFactory();
    return factory.createTripIndices(list);
  }

  public static StopTimeEntryImpl addStopTime(TripEntryImpl trip,
      StopTimeEntryImpl stopTime) {

    List<StopTimeEntry> stopTimes = trip.getStopTimes();

    if (stopTimes == null) {
      stopTimes = new ArrayList<StopTimeEntry>();
      trip.setStopTimes(stopTimes);
    }

    if (!stopTimes.isEmpty()) {
      StopTimeEntry prev = stopTimes.get(stopTimes.size() - 1);
      stopTime.setAccumulatedSlackTime(prev.getAccumulatedSlackTime()
          + prev.getSlackTime());
    }

    stopTimes.add(stopTime);
    stopTime.setTrip(trip);

    return stopTime;
  }

  public static StopTimeEntryImpl stopTime(int id, StopEntryImpl stop,
      TripEntryImpl trip, int arrivalTime, int departureTime,
      double shapeDistTraveled) {
    return stopTime(id, stop, trip, arrivalTime, departureTime,
        shapeDistTraveled, -1);
  }

  public static StopTimeEntryImpl stopTime(int id, StopEntryImpl stop,
      TripEntryImpl trip, int arrivalTime, int departureTime,
      double shapeDistTraveled, int shapeIndex) {

    StopTimeEntryImpl stopTime = new StopTimeEntryImpl();
    stopTime.setId(id);
    stopTime.setStop(stop);

    stopTime.setArrivalTime(arrivalTime);
    stopTime.setDepartureTime(departureTime);
    stopTime.setShapeDistTraveled(shapeDistTraveled);
    stopTime.setShapePointIndex(shapeIndex);

    if (trip != null)
      addStopTime(trip, stopTime);

    return stopTime;
  }

  public static StopTimeEntryImpl stopTime(int id, StopEntryImpl stop,
      TripEntryImpl trip, int time, double shapeDistTraveled) {
    return stopTime(id, stop, trip, time, time, shapeDistTraveled);
  }

  public static BlockConfigurationEntry blockConfiguration(BlockEntry block,
      ServiceIdActivation serviceIds, TripEntry... trips) {
    Builder builder = BlockConfigurationEntryImpl.builder();
    builder.setBlock(block);
    builder.setServiceIds(serviceIds);
    builder.setTrips(Arrays.asList(trips));
    builder.setTripGapDistances(new double[trips.length]);
    return builder.create();
  }

  public static BlockTripEntryImpl blockTrip(
      BlockConfigurationEntry blockConfig, TripEntry trip) {
    BlockTripEntryImpl blockTrip = new BlockTripEntryImpl();
    blockTrip.setBlockConfiguration(blockConfig);
    blockTrip.setTrip(trip);
    return blockTrip;
  }

  public static BlockStopTimeEntryImpl blockStopTime(StopTimeEntry stopTime,
      int blockSequence, BlockTripEntry trip) {
    return new BlockStopTimeEntryImpl(stopTime, blockSequence, trip);
  }

  public static LocalizedServiceId lsid(String id) {
    return new LocalizedServiceId(aid(id), timeZone());
  }

  public static List<LocalizedServiceId> lsids(String... ids) {
    List<LocalizedServiceId> serviceIds = new ArrayList<LocalizedServiceId>();
    for (String id : ids)
      serviceIds.add(lsid(id));
    return serviceIds;
  }

  public static ServiceIdActivation serviceIds(
      List<LocalizedServiceId> activeServiceIds,
      List<LocalizedServiceId> inactiveServiceIds) {
    return new ServiceIdActivation(activeServiceIds, inactiveServiceIds);
  }

  public static AgencyAndId aid(String id) {
    return new AgencyAndId("1", id);
  }

  public static ShapePoints shapePointsFromLatLons(String id, double... values) {

    if (values.length % 2 != 0)
      throw new IllegalStateException();

    int n = values.length / 2;

    double[] lats = new double[n];
    double[] lons = new double[n];
    double[] distances = new double[n];

    double distance = 0;

    for (int i = 0; i < n; i++) {
      lats[i] = values[i * 2];
      lons[i] = values[i * 2 + 1];
      if (i > 0) {
        distance += SphericalGeometryLibrary.distance(lats[i - 1], lons[i - 1],
            lats[i], lons[i]);
      }
      distances[i] = distance;
    }

    return shapePoints(id, lats, lons, distances);
  }

  public static ShapePoints shapePoints(String id, double[] lats,
      double[] lons, double[] distTraveled) {
    ShapePoints shapePoints = new ShapePoints();
    shapePoints.setShapeId(aid(id));
    shapePoints.setLats(lats);
    shapePoints.setLons(lons);
    shapePoints.setDistTraveled(distTraveled);
    return shapePoints;
  }

  public static ShapePoint shapePoint(String id, int sequence, double lat,
      double lon) {
    ShapePoint point = new ShapePoint();
    point.setId(sequence);
    point.setSequence(sequence);
    point.setLat(lat);
    point.setLon(lon);
    point.setShapeId(aid(id));
    return point;
  }

  public static void addServiceDates(CalendarServiceData data, String sid,
      ServiceDate... serviceDates) {
    AgencyAndId serviceId = aid(sid);
    LocalizedServiceId lsid = lsid(sid);

    data.putTimeZoneForAgencyId(serviceId.getAgencyId(), timeZone());
    data.putServiceDatesForServiceId(serviceId, Arrays.asList(serviceDates));

    List<Date> dates = new ArrayList<Date>();

    for (ServiceDate date : serviceDates) {
      dates.add(date.getAsDate(_timeZone));
    }

    data.putDatesForLocalizedServiceId(lsid, dates);
  }

  public static void addDates(CalendarServiceData data, String sid,
      Date... dates) {
    AgencyAndId serviceId = aid(sid);
    LocalizedServiceId lsid = lsid(sid);

    data.putTimeZoneForAgencyId(serviceId.getAgencyId(), timeZone());
    data.putDatesForLocalizedServiceId(lsid, Arrays.asList(dates));

    Calendar c = Calendar.getInstance();
    c.setTimeZone(timeZone());

    List<ServiceDate> serviceDates = new ArrayList<ServiceDate>();
    for (Date date : dates) {
      c.setTime(date);
      ServiceDate serviceDate = new ServiceDate(c);
      serviceDates.add(serviceDate);
    }

    data.putServiceDatesForServiceId(serviceId, serviceDates);
  }

}
