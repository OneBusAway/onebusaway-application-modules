/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import org.onebusaway.collections.Counter;
import org.onebusaway.collections.FactoryMap;
import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopCalendarDayBean;
import org.onebusaway.transit_data.model.StopCalendarDaysBean;
import org.onebusaway.transit_data.model.StopRouteDirectionScheduleBean;
import org.onebusaway.transit_data.model.StopRouteScheduleBean;
import org.onebusaway.transit_data.model.StopTimeGroupBean;
import org.onebusaway.transit_data.model.StopTimeInstanceBean;
import org.onebusaway.transit_data.model.schedule.FrequencyInstanceBean;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.AgencyService;
import org.onebusaway.transit_data_federation.services.ExtendedCalendarService;
import org.onebusaway.transit_data_federation.services.beans.RouteBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopScheduleBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.InstanceState;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyBlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.transit_data_federation.model.StopTimeInstance;
import org.onebusaway.utility.text.NaturalStringOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class StopScheduleBeanServiceImpl implements StopScheduleBeanService {

  private static final int DEFAULT_CONTINUES_AS_THRESHOLD = 7 * 60;

  private static StopTimeBeanComparator _stopTimeComparator = new StopTimeBeanComparator();

  private static FrequencyBeanComparator _frequencyComparator = new FrequencyBeanComparator();

  private static DirectionComparator _directionComparator = new DirectionComparator();

  private static StopRouteScheduleBeanComparator _stopRouteScheduleComparator = new StopRouteScheduleBeanComparator();

  private AgencyService _agencyService;

  private TransitGraphDao _graph;

  private ExtendedCalendarService _calendarService;

  private RouteBeanService _routeBeanService;

  private NarrativeService _narrativeService;

  private BlockIndexService _blockIndexService;

  private int _continuesAsThreshold = DEFAULT_CONTINUES_AS_THRESHOLD;

  @Autowired
  public void setAgencyService(AgencyService agencyService) {
    _agencyService = agencyService;
  }

  @Autowired
  public void setTransitGraphDao(TransitGraphDao graph) {
    _graph = graph;
  }

  @Autowired
  public void setCalendarService(ExtendedCalendarService calendarService) {
    _calendarService = calendarService;
  }

  @Autowired
  public void setRouteBeanService(RouteBeanService routeBeanService) {
    _routeBeanService = routeBeanService;
  }

  @Autowired
  public void setNarrativeService(NarrativeService narrativeService) {
    _narrativeService = narrativeService;
  }

  @Autowired
  public void setBlockIndexService(BlockIndexService blockIndexService) {
    _blockIndexService = blockIndexService;
  }

  /**
   * When determining if one trip "continues as" another, we have a time
   * threshold to determine if a rider is likely to actually stay on that bus.
   * The idea is that if the time delay between the previous trip and the next
   * trip, it's likely that there is a layover in there that the user wouldn't
   * stick around for.
   * 
   * @param continuesAsThreshold - time in seconds
   */
  public void setContinuesAsThreshold(int continuesAsThreshold) {
    _continuesAsThreshold = continuesAsThreshold;
  }

  @Cacheable
  public StopCalendarDaysBean getCalendarForStop(AgencyAndId stopId) {

    TimeZone timeZone = _agencyService.getTimeZoneForAgencyId(stopId.getAgencyId());

    StopEntry stopEntry = _graph.getStopEntryForId(stopId);
    Set<ServiceIdActivation> serviceIds = new HashSet<ServiceIdActivation>();

    for (BlockStopTimeIndex index : _blockIndexService.getStopTimeIndicesForStop(stopEntry))
      serviceIds.add(index.getServiceIds());

    for (FrequencyBlockStopTimeIndex index : _blockIndexService.getFrequencyStopTimeIndicesForStop(stopEntry))
      serviceIds.add(index.getServiceIds());

    SortedMap<ServiceDate, Set<ServiceIdActivation>> serviceIdsByDate = getServiceIdsByDate(serviceIds);

    Counter<Set<ServiceIdActivation>> counts = new Counter<Set<ServiceIdActivation>>();
    for (Set<ServiceIdActivation> ids : serviceIdsByDate.values())
      counts.increment(ids);

    int total = counts.size();
    Map<Set<ServiceIdActivation>, Integer> idsToGroup = new HashMap<Set<ServiceIdActivation>, Integer>();
    for (Set<ServiceIdActivation> ids : counts.getSortedKeys())
      idsToGroup.put(ids, total--);

    List<StopCalendarDayBean> beans = new ArrayList<StopCalendarDayBean>(
        serviceIdsByDate.size());
    for (Map.Entry<ServiceDate, Set<ServiceIdActivation>> entry : serviceIdsByDate.entrySet()) {
      StopCalendarDayBean bean = new StopCalendarDayBean();
      ServiceDate serviceDate = entry.getKey();
      Date date = serviceDate.getAsDate(timeZone);
      bean.setDate(date);
      Integer indexId = idsToGroup.get(entry.getValue());
      bean.setGroup(indexId);
      beans.add(bean);
    }

    return new StopCalendarDaysBean(timeZone.getID(), beans);
  }

  @Cacheable
  public List<StopRouteScheduleBean> getScheduledArrivalsForStopAndDate(
      AgencyAndId stopId, ServiceDate date) {

    StopEntry stopEntry = _graph.getStopEntryForId(stopId);

    Map<AgencyAndId, List<StopTimeInstance>> stopTimesByRouteCollectionId = new FactoryMap<AgencyAndId, List<StopTimeInstance>>(
        new ArrayList<StopTimeInstance>());
    Map<AgencyAndId, List<StopTimeInstance>> frequenciesByRouteCollectionId = new FactoryMap<AgencyAndId, List<StopTimeInstance>>(
        new ArrayList<StopTimeInstance>());

    groupStopTimeInstancesByRouteCollectionId(stopEntry, date,
        stopTimesByRouteCollectionId, frequenciesByRouteCollectionId);

    groupFrequencyInstancesByRouteCollectionId(stopEntry, date,
        frequenciesByRouteCollectionId);

    Set<AgencyAndId> routeIds = new HashSet<AgencyAndId>();
    routeIds.addAll(stopTimesByRouteCollectionId.keySet());
    routeIds.addAll(frequenciesByRouteCollectionId.keySet());

    List<StopRouteScheduleBean> beans = new ArrayList<StopRouteScheduleBean>();

    for (AgencyAndId routeId : routeIds) {

      StopRouteScheduleBean routeScheduleBean = new StopRouteScheduleBean();
      beans.add(routeScheduleBean);

      RouteBean route = _routeBeanService.getRouteForId(routeId);
      routeScheduleBean.setRoute(route);

      Map<String, StopTimeByDirectionEntry> stopTimesByDirection = new FactoryMap<String, StopTimeByDirectionEntry>(
          new StopTimeByDirectionEntry());

      List<StopTimeInstance> stopTimesForRoute = stopTimesByRouteCollectionId.get(routeId);

      for (StopTimeInstance sti : stopTimesForRoute) {

        BlockStopTimeEntry bst = sti.getStopTime();
        BlockTripEntry blockTrip = sti.getTrip();
        BlockConfigurationEntry blockConfig = blockTrip.getBlockConfiguration();
        TripEntry trip = blockTrip.getTrip();

        AgencyAndId tripId = trip.getId();
        AgencyAndId serviceId = trip.getServiceId().getId();

        TripNarrative narrative = _narrativeService.getTripForId(tripId);

        StopTimeInstanceBean stiBean = new StopTimeInstanceBean();
        stiBean.setTripId(AgencyAndIdLibrary.convertToString(tripId));
        stiBean.setServiceDate(sti.getServiceDate());
        stiBean.setArrivalTime(sti.getArrivalTime());
        stiBean.setDepartureTime(sti.getDepartureTime());
        stiBean.setServiceId(AgencyAndIdLibrary.convertToString(serviceId));

        stiBean.setArrivalEnabled(bst.getBlockSequence() > 0);
        stiBean.setDepartureEnabled(bst.getBlockSequence() + 1 < blockConfig.getStopTimes().size());

        String directionId = trip.getDirectionId();
        if (directionId == null)
          directionId = "0";

        String tripHeadsign = narrative.getTripHeadsign();

        TripHeadsignStopTimeGroupKey groupKey = new TripHeadsignStopTimeGroupKey(
            tripHeadsign);
        ContinuesAsStopTimeGroupKey continuesAsGroupKey = getContinuesAsGroupKeyForStopTimeInstance(sti);

        StopTimeByDirectionEntry stopTimesForDirection = stopTimesByDirection.get(directionId);

        stopTimesForDirection.addEntry(stiBean, tripHeadsign, groupKey,
            continuesAsGroupKey);
      }

      List<StopTimeInstance> frequenciesForRoute = frequenciesByRouteCollectionId.get(routeId);

      for (StopTimeInstance sti : frequenciesForRoute) {

        BlockStopTimeEntry blockStopTime = sti.getStopTime();
        BlockTripEntry blockTrip = blockStopTime.getTrip();
        TripEntry trip = blockTrip.getTrip();
        BlockConfigurationEntry blockConfig = blockTrip.getBlockConfiguration();

        AgencyAndId tripId = trip.getId();
        AgencyAndId serviceId = trip.getServiceId().getId();

        TripNarrative narrative = _narrativeService.getTripForId(tripId);

        FrequencyInstanceBean bean = new FrequencyInstanceBean();
        bean.setTripId(AgencyAndIdLibrary.convertToString(tripId));
        bean.setServiceDate(sti.getServiceDate());
        bean.setStartTime(sti.getServiceDate()
            + sti.getFrequency().getStartTime() * 1000);
        bean.setEndTime(sti.getServiceDate() + sti.getFrequency().getEndTime()
            * 1000);
        bean.setHeadwaySecs(sti.getFrequency().getHeadwaySecs());
        bean.setServiceId(AgencyAndIdLibrary.convertToString(serviceId));
        bean.setArrivalEnabled(blockStopTime.getBlockSequence() > 0);
        bean.setDepartureEnabled(blockStopTime.getBlockSequence() + 1 < blockConfig.getStopTimes().size());

        String directionId = trip.getDirectionId();
        if (directionId == null)
          directionId = "0";

        StopTimeByDirectionEntry stopTimesForDirection = stopTimesByDirection.get(directionId);
        stopTimesForDirection.addEntry(bean, narrative.getTripHeadsign());
      }

      for (StopTimeByDirectionEntry stopTimesForDirection : stopTimesByDirection.values()) {

        StopRouteDirectionScheduleBean directionBean = new StopRouteDirectionScheduleBean();

        directionBean.getStopTimes().addAll(
            stopTimesForDirection.getStopTimes());

        directionBean.getFrequencies().addAll(
            stopTimesForDirection.getFrequencies());

        String headsign = stopTimesForDirection.getBestHeadsign();
        directionBean.setTripHeadsign(headsign);

        Collections.sort(directionBean.getStopTimes(), _stopTimeComparator);
        Collections.sort(directionBean.getFrequencies(), _frequencyComparator);

        List<StopTimeGroupBean> groups = new ArrayList<StopTimeGroupBean>();

        applyTripHeadsignStopTimeGroups(stopTimesForDirection, groups);
        applyContinuesAsStopTimeGroups(stopTimesForDirection, groups);

        directionBean.setGroups(groups);

        routeScheduleBean.getDirections().add(directionBean);
      }

      Collections.sort(routeScheduleBean.getDirections(), _directionComparator);
    }

    Collections.sort(beans, _stopRouteScheduleComparator);

    return beans;
  }

  /****
   * Private Methods
   ****/

  private SortedMap<ServiceDate, Set<ServiceIdActivation>> getServiceIdsByDate(
      Set<ServiceIdActivation> allServiceIds) {

    SortedMap<ServiceDate, Set<ServiceIdActivation>> serviceIdsByDate = new TreeMap<ServiceDate, Set<ServiceIdActivation>>();
    serviceIdsByDate = FactoryMap.createSorted(serviceIdsByDate,
        new HashSet<ServiceIdActivation>());

    for (ServiceIdActivation serviceIds : allServiceIds) {
      Set<ServiceDate> dates = _calendarService.getServiceDatesForServiceIds(serviceIds);
      for (ServiceDate date : dates) {
        serviceIdsByDate.get(date).add(serviceIds);
      }
    }
    return serviceIdsByDate;
  }

  private static class StopTimeBeanComparator implements
      Comparator<StopTimeInstanceBean> {

    public int compare(StopTimeInstanceBean o1, StopTimeInstanceBean o2) {
      long t1 = o1.getDepartureTime();
      long t2 = o2.getDepartureTime();
      return new Long(t1).compareTo(new Long(t2));
    }
  }

  private static class FrequencyBeanComparator implements
      Comparator<FrequencyInstanceBean> {

    public int compare(FrequencyInstanceBean o1, FrequencyInstanceBean o2) {

      long t1 = o1.getStartTime();
      long t2 = o2.getStartTime();
      return new Long(t1).compareTo(new Long(t2));
    }
  }

  private void groupStopTimeInstancesByRouteCollectionId(StopEntry stopEntry,
      ServiceDate date,
      Map<AgencyAndId, List<StopTimeInstance>> stopTimesByRouteCollectionId,
      Map<AgencyAndId, List<StopTimeInstance>> frequenciesByRouteCollectionId) {

    Map<AgencyAndId, Set<FrequencyEntry>> frequencyLabelsByRouteCollectionId = new FactoryMap<AgencyAndId, Set<FrequencyEntry>>(
        new HashSet<FrequencyEntry>());

    for (BlockStopTimeIndex index : _blockIndexService.getStopTimeIndicesForStop(stopEntry)) {

      ServiceIdActivation serviceIds = index.getServiceIds();

      Set<ServiceDate> serviceDates = _calendarService.getServiceDatesForServiceIds(serviceIds);
      if (!serviceDates.contains(date))
        continue;

      Date serviceDate = date.getAsDate(serviceIds.getTimeZone());

      for (BlockStopTimeEntry stopTime : index.getStopTimes()) {

        BlockTripEntry blockTrip = stopTime.getTrip();
        TripEntry trip = blockTrip.getTrip();
        AgencyAndId routeCollectionId = trip.getRouteCollection().getId();

        FrequencyEntry frequencyLabel = trip.getFrequencyLabel();
        InstanceState state = new InstanceState(serviceDate.getTime(),
            frequencyLabel);
        StopTimeInstance sti = new StopTimeInstance(stopTime, state);

        if (frequencyLabel == null) {
          stopTimesByRouteCollectionId.get(routeCollectionId).add(sti);
        } else if (frequencyLabelsByRouteCollectionId.get(routeCollectionId).add(
            frequencyLabel)) {
          frequenciesByRouteCollectionId.get(routeCollectionId).add(sti);
        }
      }
    }
  }

  private void groupFrequencyInstancesByRouteCollectionId(StopEntry stopEntry,
      ServiceDate date,
      Map<AgencyAndId, List<StopTimeInstance>> frequenciesByRouteCollectionId) {

    for (FrequencyBlockStopTimeIndex index : _blockIndexService.getFrequencyStopTimeIndicesForStop(stopEntry)) {

      ServiceIdActivation serviceIds = index.getServiceIds();

      Set<ServiceDate> serviceDates = _calendarService.getServiceDatesForServiceIds(serviceIds);
      if (!serviceDates.contains(date))
        continue;

      Date serviceDate = date.getAsDate(serviceIds.getTimeZone());

      for (FrequencyBlockStopTimeEntry entry : index.getFrequencyStopTimes()) {

        BlockStopTimeEntry stopTime = entry.getStopTime();

        BlockTripEntry blockTrip = stopTime.getTrip();
        TripEntry trip = blockTrip.getTrip();
        AgencyAndId routeCollectionId = trip.getRouteCollection().getId();
        InstanceState state = new InstanceState(serviceDate.getTime(),
            entry.getFrequency());

        StopTimeInstance sti = new StopTimeInstance(stopTime, state);

        frequenciesByRouteCollectionId.get(routeCollectionId).add(sti);
      }
    }
  }

  private ContinuesAsStopTimeGroupKey getContinuesAsGroupKeyForStopTimeInstance(
      StopTimeInstance instance) {

    BlockTripEntry blockTrip = instance.getTrip();
    AgencyAndId lineId = getContinuesAsLineId(blockTrip);
    return new ContinuesAsStopTimeGroupKey(lineId);
  }

  private AgencyAndId getContinuesAsLineId(BlockTripEntry blockTrip) {

    BlockTripEntry nextTrip = blockTrip.getNextTrip();
    if (nextTrip == null)
      return null;

    TripEntry prevTrip = blockTrip.getTrip();
    AgencyAndId prevLineId = prevTrip.getRouteCollection().getId();
    AgencyAndId nextLineId = nextTrip.getTrip().getRouteCollection().getId();
    if (prevLineId.equals(nextLineId))
      return null;

    List<BlockStopTimeEntry> stopTimes = blockTrip.getStopTimes();
    BlockStopTimeEntry prevStopTime = stopTimes.get(stopTimes.size() - 1);

    List<BlockStopTimeEntry> nextStopTimes = nextTrip.getStopTimes();
    BlockStopTimeEntry nextStopTime = nextStopTimes.get(0);

    int prevTime = prevStopTime.getStopTime().getDepartureTime();
    int nextTime = nextStopTime.getStopTime().getArrivalTime();

    if (nextTime - prevTime > _continuesAsThreshold)
      return null;

    return nextLineId;
  }

  private void applyTripHeadsignStopTimeGroups(
      StopTimeByDirectionEntry stopTimesForDirection,
      List<StopTimeGroupBean> groups) {

    Counter<TripHeadsignStopTimeGroupKey> keyCounts = stopTimesForDirection.getTripHeadsignKeyCounts();
    List<TripHeadsignStopTimeGroupKey> sortedKeys = keyCounts.getSortedKeys();

    for (int i = 0; i < sortedKeys.size() - 1; i++) {

      TripHeadsignStopTimeGroupKey key = sortedKeys.get(i);
      StopTimeGroupBean group = new StopTimeGroupBean();

      String groupId = Integer.toString(groups.size());
      group.setId(groupId);

      String tripHeadsign = key.getTripHeadsign();
      group.setTripHeadsign(tripHeadsign);

      applyGroupIdForGroupKey(stopTimesForDirection, key, groupId);

      groups.add(group);
    }
  }

  private void applyContinuesAsStopTimeGroups(
      StopTimeByDirectionEntry stopTimesForDirection,
      List<StopTimeGroupBean> groups) {

    Counter<ContinuesAsStopTimeGroupKey> keyCounts = stopTimesForDirection.getContinuesAsKeyCounts();
    List<ContinuesAsStopTimeGroupKey> sortedKeys = keyCounts.getSortedKeys();

    for (ContinuesAsStopTimeGroupKey key : sortedKeys) {

      AgencyAndId lineId = key.getLineId();
      if (lineId == null)
        continue;
      StopTimeGroupBean group = new StopTimeGroupBean();

      String groupId = Integer.toString(groups.size());
      group.setId(groupId);

      RouteBean route = _routeBeanService.getRouteForId(lineId);
      group.setContinuesAs(route);

      applyGroupIdForGroupKey(stopTimesForDirection, key, groupId);

      groups.add(group);
    }
  }

  private void applyGroupIdForGroupKey(
      StopTimeByDirectionEntry stopTimesForDirection, Object key, String groupId) {

    List<StopTimeInstanceBean> stopTimesForGroup = stopTimesForDirection.getStopTimesForGroupKey(key);

    for (StopTimeInstanceBean stiBean : stopTimesForGroup) {

      List<String> groupIds = stiBean.getGroupIds();
      if (groupIds == null) {
        groupIds = new ArrayList<String>();
        stiBean.setGroupIds(groupIds);
      }
      groupIds.add(groupId);
    }
  }

  private static class StopRouteScheduleBeanComparator implements
      Comparator<StopRouteScheduleBean> {

    public int compare(StopRouteScheduleBean o1, StopRouteScheduleBean o2) {
      String a = getNameForRoute(o1.getRoute());
      String b = getNameForRoute(o2.getRoute());
      return NaturalStringOrder.compareNatural(a, b);
    }

    private static String getNameForRoute(RouteBean route) {
      String name = route.getShortName();
      if (name == null)
        name = route.getLongName();
      if (name == null)
        name = route.getId();
      return name;
    }
  }

  private static class DirectionComparator implements
      Comparator<StopRouteDirectionScheduleBean> {
    @Override
    public int compare(StopRouteDirectionScheduleBean o1,
        StopRouteDirectionScheduleBean o2) {
      String tripA = o1.getTripHeadsign();
      String tripB = o2.getTripHeadsign();
      if (tripA == null)
        tripA = "";
      if (tripB == null)
        tripB = "";
      return tripA.compareTo(tripB);
    }
  }

  public static class StopTimeByDirectionEntry {

    private List<StopTimeInstanceBean> _stopTimes = new ArrayList<StopTimeInstanceBean>();

    private List<FrequencyInstanceBean> _frequencies = new ArrayList<FrequencyInstanceBean>();

    private Counter<String> _headsigns = new Counter<String>();

    private Counter<TripHeadsignStopTimeGroupKey> _tripHeadsignKeyCounts = new Counter<TripHeadsignStopTimeGroupKey>();

    private Counter<ContinuesAsStopTimeGroupKey> _continuesAsKeyCounts = new Counter<ContinuesAsStopTimeGroupKey>();

    private Map<Object, List<StopTimeInstanceBean>> _stopTimesByGroupKey = new FactoryMap<Object, List<StopTimeInstanceBean>>(
        new ArrayList<StopTimeInstanceBean>());

    public Collection<? extends StopTimeInstanceBean> getStopTimes() {
      return _stopTimes;
    }

    public Collection<FrequencyInstanceBean> getFrequencies() {
      return _frequencies;
    }

    public void addEntry(StopTimeInstanceBean sti, String headsign,
        TripHeadsignStopTimeGroupKey groupKey,
        ContinuesAsStopTimeGroupKey continuesAsGroupKey) {
      _stopTimes.add(sti);
      _headsigns.increment(headsign);
      _tripHeadsignKeyCounts.increment(groupKey);
      _continuesAsKeyCounts.increment(continuesAsGroupKey);
      _stopTimesByGroupKey.get(groupKey).add(sti);
      _stopTimesByGroupKey.get(continuesAsGroupKey).add(sti);
    }

    public void addEntry(FrequencyInstanceBean fi, String headsign) {
      _frequencies.add(fi);

      // We weight the frequency-based headsign count by the estimated number of
      // trips in the interval
      int rangeInSeconds = (int) ((fi.getEndTime() - fi.getStartTime()) / 1000);
      int count = rangeInSeconds / fi.getHeadwaySecs();

      _headsigns.increment(headsign, count);
    }

    public String getBestHeadsign() {
      return _headsigns.getMax();
    }

    public Counter<TripHeadsignStopTimeGroupKey> getTripHeadsignKeyCounts() {
      return _tripHeadsignKeyCounts;
    }

    public Counter<ContinuesAsStopTimeGroupKey> getContinuesAsKeyCounts() {
      return _continuesAsKeyCounts;
    }

    public List<StopTimeInstanceBean> getStopTimesForGroupKey(Object key) {
      return _stopTimesByGroupKey.get(key);
    }
  }

  private static class TripHeadsignStopTimeGroupKey {
    private final String tripHeadsign;

    public TripHeadsignStopTimeGroupKey(String tripHeadsign) {
      this.tripHeadsign = tripHeadsign;
    }

    public String getTripHeadsign() {
      return tripHeadsign;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
          + ((tripHeadsign == null) ? 0 : tripHeadsign.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      TripHeadsignStopTimeGroupKey other = (TripHeadsignStopTimeGroupKey) obj;
      if (tripHeadsign == null) {
        if (other.tripHeadsign != null)
          return false;
      } else if (!tripHeadsign.equals(other.tripHeadsign))
        return false;
      return true;
    }
  }

  private static class ContinuesAsStopTimeGroupKey {

    private final AgencyAndId _lineId;

    public ContinuesAsStopTimeGroupKey(AgencyAndId lineId) {
      _lineId = lineId;
    }

    public AgencyAndId getLineId() {
      return _lineId;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_lineId == null) ? 0 : _lineId.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      ContinuesAsStopTimeGroupKey other = (ContinuesAsStopTimeGroupKey) obj;
      if (_lineId == null) {
        if (other._lineId != null)
          return false;
      } else if (!_lineId.equals(other._lineId))
        return false;
      return true;
    }
  }
}
