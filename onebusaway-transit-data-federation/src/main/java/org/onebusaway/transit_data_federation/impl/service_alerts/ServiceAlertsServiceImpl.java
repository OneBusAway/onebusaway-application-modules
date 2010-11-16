package org.onebusaway.transit_data_federation.impl.service_alerts;

import java.io.File;
import java.util.ArrayList;
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
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlertsService;
import org.onebusaway.transit_data_federation.services.service_alerts.Situation;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ServiceAlertsServiceImpl implements ServiceAlertsService {

  private static Logger _log = LoggerFactory.getLogger(ServiceAlertsServiceImpl.class);

  private ConcurrentMap<AgencyAndId, Situation> _situations = new ConcurrentHashMap<AgencyAndId, Situation>();

  private ConcurrentMap<String, Set<AgencyAndId>> _situationIdsByAgencyId = new ConcurrentHashMap<String, Set<AgencyAndId>>();

  private ConcurrentMap<AgencyAndId, Set<AgencyAndId>> _situationsByStopId = new ConcurrentHashMap<AgencyAndId, Set<AgencyAndId>>();

  private ConcurrentMap<AgencyAndId, Set<AgencyAndId>> _situationIdsByLineId = new ConcurrentHashMap<AgencyAndId, Set<AgencyAndId>>();

  private ConcurrentMap<LineAndDirectionRef, Set<AgencyAndId>> _situationIdsByLineAndDirectionId = new ConcurrentHashMap<LineAndDirectionRef, Set<AgencyAndId>>();

  private File _path;

  public void setPath(File path) {
    _path = path;
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
    updateReferences(situation);
    saveServiceAlerts();
  }

  @Override
  public void removeServiceAlert(AgencyAndId situationId) {

    Situation existingSituation = _situations.remove(situationId);

    if (existingSituation != null) {
      updateReferences(existingSituation, null);
    }
  }

  @Override
  public Situation getServiceAlertForId(AgencyAndId situationId) {
    return _situations.get(situationId);
  }

  @Override
  public List<Situation> getAllSituationsForAgencyId(String agencyId) {
    Set<AgencyAndId> situationIds = _situationIdsByAgencyId.get(agencyId);
    return getSituationIdsAsObjects(situationIds);
  }

  @Override
  public List<Situation> getSituationsForStopId(long time, AgencyAndId stopId) {

    Set<AgencyAndId> situationIds = _situationsByStopId.get(stopId);
    return getSituationIdsAsObjects(situationIds);
  }

  @Override
  public List<Situation> getSituationsForStopCall(long time,
      BlockInstance blockInstance, BlockStopTimeEntry blockStopTime,
      AgencyAndId vehicleId) {

    Set<AgencyAndId> situationIds = new HashSet<AgencyAndId>();
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

    updateReferences(existingSituation, situation, _situationIdsByAgencyId,
        AffectsAgencyKeyFactory.INSTANCE);

    updateReferences(existingSituation, situation, _situationsByStopId,
        AffectsStopKeyFactory.INSTANCE);

    updateReferences(existingSituation, situation, _situationIdsByLineId,
        AffectsLineKeyFactory.INSTANCE);

    updateReferences(existingSituation, situation,
        _situationIdsByLineAndDirectionId,
        AffectsLineAndDirectionKeyFactory.INSTANCE);
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

  private void loadServieAlerts() {

    if (_path == null || !_path.exists())
      return;

    try {

      List<Situation> situations = ObjectSerializationLibrary.readObject(_path);
      for (Situation situation : situations)
        updateReferences(situation);

    } catch (Exception ex) {
      _log.error("error loading service alerts from path " + _path, ex);
    }
  }

  private void saveServiceAlerts() {
    if (_path == null)
      return;

    try {
      List<Situation> situations = new ArrayList<Situation>(
          _situations.values());
      ObjectSerializationLibrary.writeObject(_path, situations);
    } catch (Exception ex) {
      _log.error("error saving service alerts to path " + _path, ex);
    }
  }

}
