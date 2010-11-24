package org.onebusaway.transit_data_federation.impl.service_alerts;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.collections.ConcurrentCollectionsLibrary;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlertsService;
import org.onebusaway.transit_data_federation.services.service_alerts.Situation;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedCall;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedStop;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedVehicleJourney;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffects;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationConditionDetails;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationConsequence;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationsContainer;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.thoughtworks.xstream.XStream;

@Component
public class ServiceAlertsServiceImpl implements ServiceAlertsService {

  private static Logger _log = LoggerFactory.getLogger(ServiceAlertsServiceImpl.class);

  private ConcurrentMap<AgencyAndId, Situation> _situations = new ConcurrentHashMap<AgencyAndId, Situation>();

  private ConcurrentMap<String, Set<AgencyAndId>> _situationIdsBySituationAgencyId = new ConcurrentHashMap<String, Set<AgencyAndId>>();

  private ConcurrentMap<String, Set<AgencyAndId>> _situationIdsByAgencyId = new ConcurrentHashMap<String, Set<AgencyAndId>>();

  private ConcurrentMap<AgencyAndId, Set<AgencyAndId>> _situationIdsByStopId = new ConcurrentHashMap<AgencyAndId, Set<AgencyAndId>>();

  private ConcurrentMap<AgencyAndId, Set<AgencyAndId>> _situationIdsByLineId = new ConcurrentHashMap<AgencyAndId, Set<AgencyAndId>>();

  private ConcurrentMap<LineAndDirectionRef, Set<AgencyAndId>> _situationIdsByLineAndDirectionId = new ConcurrentHashMap<LineAndDirectionRef, Set<AgencyAndId>>();

  private ConcurrentMap<LineAndStopCallRef, Set<AgencyAndId>> _situationIdsByLineAndStopCall = new ConcurrentHashMap<LineAndStopCallRef, Set<AgencyAndId>>();

  private ConcurrentMap<LineDirectionAndStopCallRef, Set<AgencyAndId>> _situationIdsByLineDirectionAndStopCall = new ConcurrentHashMap<LineDirectionAndStopCallRef, Set<AgencyAndId>>();

  private FederatedTransitDataBundle _bundle;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  @PostConstruct
  public void start() {
    loadServieAlerts();
  }

  @PreDestroy
  public void stop() {
    saveServiceAlerts();
  }

  /****
   * {@link ServiceAlertsService} Interface
   ****/

  @Override
  public Situation createServiceAlert(String agencyId, Situation situation) {

    AgencyAndId id = new AgencyAndId(agencyId,
        Long.toString(System.currentTimeMillis()));
    situation.setId(id);

    if (situation.getCreationTime() == 0)
      situation.setCreationTime(System.currentTimeMillis());

    updateReferences(situation);
    saveServiceAlerts();

    return situation;
  }

  @Override
  public void updateServiceAlert(Situation situation) {
    updateServiceAlerts(Arrays.asList(situation));
    saveServiceAlerts();
  }

  @Override
  public void updateServiceAlerts(List<Situation> situations) {
    for (Situation situation : situations)
      updateReferences(situation);
    saveServiceAlerts();
  }

  @Override
  public void removeServiceAlert(AgencyAndId situationId) {
    removeServiceAlerts(Arrays.asList(situationId));
  }

  @Override
  public void removeServiceAlerts(List<AgencyAndId> situationIds) {

    for (AgencyAndId situationId : situationIds) {

      Situation existingSituation = _situations.remove(situationId);

      if (existingSituation != null) {
        updateReferences(existingSituation, null);
      }
    }

    saveServiceAlerts();
  }

  @Override
  public Situation getServiceAlertForId(AgencyAndId situationId) {
    return _situations.get(situationId);
  }

  @Override
  public List<Situation> getAllSituationsForAgencyId(String agencyId) {
    Set<AgencyAndId> situationIds = _situationIdsBySituationAgencyId.get(agencyId);
    return getSituationIdsAsObjects(situationIds);
  }

  @Override
  public List<Situation> getSituationsForStopId(long time, AgencyAndId stopId) {

    Set<AgencyAndId> situationIds = new HashSet<AgencyAndId>();
    getSituationIdsForKey(_situationIdsByAgencyId, stopId.getAgencyId(),
        situationIds);
    getSituationIdsForKey(_situationIdsByStopId, stopId, situationIds);
    return getSituationIdsAsObjects(situationIds);
  }

  @Override
  public List<Situation> getSituationsForStopCall(long time,
      BlockInstance blockInstance, BlockStopTimeEntry blockStopTime,
      AgencyAndId vehicleId) {

    BlockTripEntry blockTrip = blockStopTime.getTrip();
    TripEntry trip = blockTrip.getTrip();
    AgencyAndId lineId = trip.getRouteCollectionId();
    String directionId = trip.getDirectionId();
    StopTimeEntry stopTime = blockStopTime.getStopTime();
    StopEntry stop = stopTime.getStop();
    AgencyAndId stopId = stop.getId();

    LineAndStopCallRef lineAndStopCallRef = new LineAndStopCallRef(lineId,
        stopId);

    Set<AgencyAndId> situationIds = new HashSet<AgencyAndId>();
    getSituationIdsForKey(_situationIdsByAgencyId, lineId.getAgencyId(),
        situationIds);
    getSituationIdsForKey(_situationIdsByLineId, lineId, situationIds);
    getSituationIdsForKey(_situationIdsByLineAndStopCall, lineAndStopCallRef,
        situationIds);

    /**
     * Remember that direction is optional
     */
    if (directionId != null) {
      LineAndDirectionRef lineAndDirectionRef = new LineAndDirectionRef(lineId,
          directionId);
      LineDirectionAndStopCallRef lineDirectionAndStopCallRef = new LineDirectionAndStopCallRef(
          lineId, directionId, stopId);

      getSituationIdsForKey(_situationIdsByLineAndDirectionId,
          lineAndDirectionRef, situationIds);
      getSituationIdsForKey(_situationIdsByLineDirectionAndStopCall,
          lineDirectionAndStopCallRef, situationIds);
    }
    return getSituationIdsAsObjects(situationIds);
  }

  @Override
  public List<Situation> getSituationsForVehicleJourney(long time,
      BlockInstance blockInstance, BlockTripEntry blockTrip,
      AgencyAndId vehicleId) {

    TripEntry trip = blockTrip.getTrip();
    AgencyAndId lineId = trip.getRouteCollectionId();
    LineAndDirectionRef lineAndDirectionRef = new LineAndDirectionRef(lineId,
        trip.getDirectionId());

    Set<AgencyAndId> situationIds = new HashSet<AgencyAndId>();
    getSituationIdsForKey(_situationIdsByAgencyId, lineId.getAgencyId(),
        situationIds);
    getSituationIdsForKey(_situationIdsByLineId, lineId, situationIds);
    getSituationIdsForKey(_situationIdsByLineAndDirectionId,
        lineAndDirectionRef, situationIds);
    return getSituationIdsAsObjects(situationIds);
  }

  /****
   * Private Methods
   ****/

  private void updateReferences(Situation situation) {

    AgencyAndId id = situation.getId();
    Situation existingSituation = _situations.put(id, situation);

    updateReferences(existingSituation, situation);
  }

  private void updateReferences(Situation existingSituation, Situation situation) {

    updateReferences(existingSituation, situation,
        _situationIdsBySituationAgencyId,
        AffectsSituationAgencyKeyFactory.INSTANCE);

    updateReferences(existingSituation, situation, _situationIdsByAgencyId,
        AffectsAgencyKeyFactory.INSTANCE);

    updateReferences(existingSituation, situation, _situationIdsByStopId,
        AffectsStopKeyFactory.INSTANCE);

    updateReferences(existingSituation, situation, _situationIdsByLineId,
        AffectsLineKeyFactory.INSTANCE);

    updateReferences(existingSituation, situation,
        _situationIdsByLineAndDirectionId,
        AffectsLineAndDirectionKeyFactory.INSTANCE);

    updateReferences(existingSituation, situation,
        _situationIdsByLineAndStopCall,
        AffectsLineAndStopCallKeyFactory.INSTANCE);

    updateReferences(existingSituation, situation,
        _situationIdsByLineDirectionAndStopCall,
        AffectsLineDirectionAndStopCallKeyFactory.INSTANCE);
  }

  private <T> void updateReferences(Situation existingSituation,
      Situation situation, ConcurrentMap<T, Set<AgencyAndId>> map,
      AffectsKeyFactory<T> affectsKeyFactory) {

    Set<T> existingEffects = Collections.emptySet();
    if (existingSituation != null && existingSituation.getAffects() != null)
      existingEffects = affectsKeyFactory.getKeysForAffects(situation,
          existingSituation.getAffects());

    Set<T> newEffects = Collections.emptySet();
    if (situation != null && situation.getAffects() != null)
      newEffects = affectsKeyFactory.getKeysForAffects(situation,
          situation.getAffects());

    for (T existingEffect : existingEffects) {
      if (newEffects.contains(existingEffect))
        continue;
      ConcurrentCollectionsLibrary.removeFromMapValueSet(map, existingEffect,
          existingSituation.getId());
    }

    for (T newEffect : newEffects) {
      if (existingEffects.contains(newEffect))
        continue;
      ConcurrentCollectionsLibrary.addToMapValueSet(map, newEffect,
          situation.getId());
    }
  }

  private <T> void getSituationIdsForKey(
      ConcurrentMap<T, Set<AgencyAndId>> situationIdsByKey, T key,
      Collection<AgencyAndId> matches) {
    Set<AgencyAndId> ids = situationIdsByKey.get(key);
    if (ids != null)
      matches.addAll(ids);
  }

  private List<Situation> getSituationIdsAsObjects(
      Collection<AgencyAndId> situationIds) {
    if (situationIds == null || situationIds.isEmpty())
      return Collections.emptyList();
    List<Situation> situations = new ArrayList<Situation>(situationIds.size());
    for (AgencyAndId situationId : situationIds) {
      Situation situation = _situations.get(situationId);
      if (situation != null)
        situations.add(situation);
    }
    return situations;
  }

  /****
   * Serialization
   ****/

  private synchronized void loadServieAlerts() {

    File path = _bundle.getServiceAlertsPath();

    if (path == null || !path.exists())
      return;

    try {

      XStream xstream = createXStream();
      FileReader in = new FileReader(path);
      SituationsContainer container = (SituationsContainer) xstream.fromXML(in);
      in.close();
      List<Situation> situations = container.getSituations();
      for (Situation situation : situations)
        updateReferences(situation);

    } catch (Exception ex) {
      _log.error("error loading service alerts from path " + path, ex);
    }
  }

  private synchronized void saveServiceAlerts() {

    File path = _bundle.getServiceAlertsPath();

    if (path == null)
      return;

    try {

      List<Situation> situations = new ArrayList<Situation>(
          _situations.values());
      SituationsContainer container = new SituationsContainer();
      container.setSituations(situations);

      XStream xstream = createXStream();

      FileWriter out = new FileWriter(path);
      xstream.toXML(container, out);
      out.close();
    } catch (Exception ex) {
      _log.error("error saving service alerts to path " + path, ex);
    }
  }

  private XStream createXStream() {

    XStream xstream = new XStream();

    xstream.alias("situationContainer", SituationsContainer.class);
    xstream.alias("situation", Situation.class);
    xstream.alias("affects", SituationAffects.class);
    xstream.alias("stop", SituationAffectedStop.class);
    xstream.alias("vehicleJourney", SituationAffectedVehicleJourney.class);
    xstream.alias("call", SituationAffectedCall.class);
    xstream.alias("consequence", SituationConsequence.class);
    xstream.alias("conditionDetails", SituationConditionDetails.class);
    xstream.alias("agencyAndId", AgencyAndId.class);
    xstream.alias("encodedPolyline", EncodedPolylineBean.class);

    return xstream;
  }
}
