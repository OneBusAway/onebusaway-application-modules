package org.onebusaway.transit_data_federation.impl.service_alerts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.onebusaway.collections.ConcurrentCollectionsLibrary;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedVehicleJourneyBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;
import org.onebusaway.transit_data.model.service_alerts.SituationExchangeDeliveryBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlertsService;
import org.springframework.stereotype.Component;

@Component
public class ServiceAlertsServiceImpl implements ServiceAlertsService {

  private ConcurrentMap<String, SituationBean> _situations = new ConcurrentHashMap<String, SituationBean>();

  private ConcurrentMap<String, Set<String>> _situationIdsByLineId = new ConcurrentHashMap<String, Set<String>>();

  @Override
  public SituationBean createServiceAlert(String agencyId,
      SituationBean situation) {

    String id = agencyId + "_" + System.currentTimeMillis();
    situation.setId(id);

    if (situation.getCreationTime() == 0)
      situation.setCreationTime(System.currentTimeMillis());

    updateReferences(situation);
    return situation;
  }

  @Override
  public void updateServiceAlert(SituationBean situation) {
    updateReferences(situation);
  }

  @Override
  public void updateServiceAlerts(SituationExchangeDeliveryBean alerts) {

    List<SituationBean> situations = alerts.getSituations();

    if (situations == null || situations.isEmpty())
      return;

    for (SituationBean situation : situations) {
      updateReferences(situation);
    }
  }

  @Override
  public SituationBean getServiceAlertForId(String situationId) {
    return _situations.get(situationId);
  }

  @Override
  public ListBean<SituationBean> getServiceAlerts(SituationQueryBean query) {
    List<SituationBean> situations = new ArrayList<SituationBean>(
        _situations.values());

    return new ListBean<SituationBean>(situations, false);
  }

  @Override
  public List<SituationBean> getSituationsForLineId(String lineId) {

    List<SituationBean> situations = new ArrayList<SituationBean>();

    Set<String> situationIds = _situationIdsByLineId.get(lineId);
    if (situationIds != null) {
      for (String situationId : situationIds) {
        SituationBean situation = _situations.get(situationId);
        if (situation != null)
          situations.add(situation);
      }
    }

    return situations;
  }

  /****
   * Private Methods
   ****/

  private void updateReferences(SituationBean situation) {

    String id = situation.getId();
    SituationBean existingSituation = _situations.put(id, situation);

    Set<String> existingLineIds = Collections.emptySet();
    if (existingSituation != null)
      existingLineIds = getVehicleJourneysAsLineIds(existingSituation);

    Set<String> newLineIds = getVehicleJourneysAsLineIds(situation);

    for (String existingLineId : existingLineIds) {
      if (newLineIds.contains(existingLineId))
        continue;
      ConcurrentCollectionsLibrary.removeFromMapValueSet(_situationIdsByLineId,
          existingLineId, id);
    }

    for (String newLineId : newLineIds) {
      if (existingLineIds.contains(newLineId))
        continue;
      ConcurrentCollectionsLibrary.addToMapValueSet(_situationIdsByLineId,
          newLineId, id);
    }
  }

  private Set<String> getVehicleJourneysAsLineIds(SituationBean situation) {
    Set<String> lineIds = new HashSet<String>();
    SituationAffectsBean affects = situation.getAffects();
    if (affects != null) {
      List<SituationAffectedVehicleJourneyBean> journeys = affects.getVehicleJourneys();
      if (journeys != null) {
        for (SituationAffectedVehicleJourneyBean journey : journeys)
          lineIds.add(journey.getLineId());
      }
    }
    return lineIds;
  }
}
