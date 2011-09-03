/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data_federation.impl.realtime.siri;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.siri.OneBusAwayConsequence;
import org.onebusaway.siri.core.SiriClientRequest;
import org.onebusaway.siri.core.SiriClient;
import org.onebusaway.transit_data.model.service_alerts.ESensitivity;
import org.onebusaway.transit_data.model.service_alerts.ESeverity;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlertsService;
import org.onebusaway.transit_data_federation.services.service_alerts.Situation;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedCall;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedVehicleJourney;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffects;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationConditionDetails;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationConsequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import uk.org.siri.siri.AffectedCallStructure;
import uk.org.siri.siri.AffectedVehicleJourneyStructure;
import uk.org.siri.siri.AffectedVehicleJourneyStructure.Calls;
import uk.org.siri.siri.AffectsScopeStructure;
import uk.org.siri.siri.AffectsScopeStructure.VehicleJourneys;
import uk.org.siri.siri.DefaultedTextStructure;
import uk.org.siri.siri.ExtensionsStructure;
import uk.org.siri.siri.PtConsequenceStructure;
import uk.org.siri.siri.PtConsequencesStructure;
import uk.org.siri.siri.PtSituationElementStructure;
import uk.org.siri.siri.SensitivityEnumeration;
import uk.org.siri.siri.ServiceDelivery;
import uk.org.siri.siri.ServiceRequest;
import uk.org.siri.siri.SeverityEnumeration;
import uk.org.siri.siri.Siri;
import uk.org.siri.siri.SituationExchangeDeliveryStructure;
import uk.org.siri.siri.SituationExchangeDeliveryStructure.Situations;

public class SiriClientService {

  private static Logger _log = LoggerFactory.getLogger(SiriClientService.class);

  private String _url;

  private SiriClient _client;

  private String _identity;

  private ScheduledExecutorService _executor;

  private Set<AgencyAndId> _currentSituationIds = new HashSet<AgencyAndId>();

  private ServiceAlertsService _serviceAlertsService;

  @Autowired
  public void setServiceAlertService(ServiceAlertsService serviceAlertsService) {
    _serviceAlertsService = serviceAlertsService;
  }

  public void setUrl(String url) {
    _url = url;
  }

  public void setIdentity(String identity) {
    _identity = identity;
  }

  @PostConstruct
  public void start() {

    _client = new SiriClient();

    if (_identity != null)
      _client.setIdentity(_identity);

    _executor = Executors.newSingleThreadScheduledExecutor();
    _executor.scheduleAtFixedRate(new ClientTask(), 0, 1, TimeUnit.MINUTES);
  }

  @PreDestroy
  public void stop() {
    _executor.shutdown();
  }

  /****
   * Private Methods
   ****/

  private synchronized void processDelivery(ServiceDelivery delivery) {

    Set<AgencyAndId> updatedSituationIds = new HashSet<AgencyAndId>();
    List<Situation> situationsToUpdate = new ArrayList<Situation>();

    for (SituationExchangeDeliveryStructure sxDelivery : delivery.getSituationExchangeDelivery()) {

      Situations situations = sxDelivery.getSituations();

      if (situations == null)
        continue;

      for (PtSituationElementStructure ptSituation : situations.getPtSituationElement()) {
        Situation situation = getPtSituationAsSituation(ptSituation);
        updatedSituationIds.add(situation.getId());
        situationsToUpdate.add(situation);
      }
    }

    _serviceAlertsService.updateServiceAlerts(situationsToUpdate);

    /**
     * Remove stale situations
     */
    List<AgencyAndId> situationIdsToRemove = new ArrayList<AgencyAndId>();
    for (AgencyAndId situationId : _currentSituationIds) {
      if (!updatedSituationIds.contains(situationId))
        situationIdsToRemove.add(situationId);

    }
    _serviceAlertsService.removeServiceAlerts(situationIdsToRemove);

    _currentSituationIds = updatedSituationIds;
  }

  private Situation getPtSituationAsSituation(
      PtSituationElementStructure ptSituation) {

    Situation situation = new Situation();
    situation.setId(AgencyAndIdLibrary.convertFromString(ptSituation.getParticipantRef().getValue()));

    handleDescriptions(ptSituation, situation);
    handlReasons(ptSituation, situation);
    handleOtherFields(ptSituation, situation);
    handleAffects(ptSituation, situation);
    handleConsequences(ptSituation, situation);

    return situation;
  }

  private void handleDescriptions(PtSituationElementStructure ptSituation,
      Situation situation) {
    situation.setSummary(nls(ptSituation.getSummary()));
    situation.setDescription(nls(ptSituation.getDescription()));
    situation.setAdvice(nls(ptSituation.getAdvice()));
    situation.setDetail(nls(ptSituation.getDetail()));
  }

  private void handlReasons(PtSituationElementStructure ptSituation,
      Situation situation) {
    if (ptSituation.getEnvironmentReason() != null)
      situation.setEnvironmentReason(ptSituation.getEnvironmentReason().value());

    if (ptSituation.getEquipmentReason() != null)
      situation.setEquipmentReason(ptSituation.getEquipmentReason().value());

    if (ptSituation.getMiscellaneousReason() != null)
      situation.setMiscellaneousReason(ptSituation.getMiscellaneousReason().value());

    if (ptSituation.getPersonnelReason() != null)
      situation.setMiscellaneousReason(ptSituation.getPersonnelReason().value());

    situation.setUndefinedReason(ptSituation.getUndefinedReason());
  }

  private void handleOtherFields(PtSituationElementStructure ptSituation,
      Situation situation) {

    SensitivityEnumeration sensitivity = ptSituation.getSensitivity();
    if (sensitivity != null) {
      ESensitivity sensitivityEnum = ESensitivity.valueOfXmlId(sensitivity.value());
      situation.setSensitivity(sensitivityEnum);
    }

    SeverityEnumeration severity = ptSituation.getSeverity();
    if (severity != null) {
      ESeverity severityEnum = ESeverity.valueOfTpegCode(severity.value());
      situation.setSeverity(severityEnum);
    }
  }

  /****
   * Affects
   ****/

  private void handleAffects(PtSituationElementStructure ptSituation,
      Situation situation) {

    AffectsScopeStructure affectsStructure = ptSituation.getAffects();

    if (affectsStructure != null) {

      SituationAffects situationAffects = new SituationAffects();
      situation.setAffects(situationAffects);

      VehicleJourneys vjs = affectsStructure.getVehicleJourneys();
      if (vjs != null
          && !CollectionsLibrary.isEmpty(vjs.getAffectedVehicleJourney())) {

        List<SituationAffectedVehicleJourney> avjs = new ArrayList<SituationAffectedVehicleJourney>();

        for (AffectedVehicleJourneyStructure vj : vjs.getAffectedVehicleJourney()) {

          SituationAffectedVehicleJourney avj = new SituationAffectedVehicleJourney();

          if (vj.getLineRef() != null)
            avj.setLineId(AgencyAndIdLibrary.convertFromString(vj.getLineRef().getValue()));

          if (vj.getDirectionRef() != null)
            avj.setDirectionId(vj.getDirectionRef().getValue());

          Calls calls = vj.getCalls();

          if (calls != null && !CollectionsLibrary.isEmpty(calls.getCall())) {
            List<SituationAffectedCall> stopIds = new ArrayList<SituationAffectedCall>();
            for (AffectedCallStructure call : calls.getCall()) {
              AgencyAndId stopId = AgencyAndIdLibrary.convertFromString(call.getStopPointRef().getValue());
              SituationAffectedCall ac = new SituationAffectedCall();
              ac.setStopId(stopId);
              stopIds.add(ac);
            }
            avj.setCalls(stopIds);
          }

          avjs.add(avj);
        }

        situationAffects.setVehicleJourneys(avjs);
      }
    }
  }

  private void handleConsequences(PtSituationElementStructure ptSituation,
      Situation situation) {

    PtConsequencesStructure consequences = ptSituation.getConsequences();

    if (consequences == null || consequences.getConsequence() == null)
      return;

    List<SituationConsequence> results = new ArrayList<SituationConsequence>();

    for (PtConsequenceStructure consequence : consequences.getConsequence()) {
      SituationConsequence result = new SituationConsequence();
      if (consequence.getCondition() != null)
        result.setCondition(consequence.getCondition().value());
      ExtensionsStructure extensions = consequence.getExtensions();
      if (extensions != null) {
        Object obj = extensions.getAny();
        if (obj instanceof OneBusAwayConsequence) {
          OneBusAwayConsequence obaConsequence = (OneBusAwayConsequence) obj;
          SituationConditionDetails details = new SituationConditionDetails();
          if (obaConsequence.getDiversionPath() != null) {
            EncodedPolylineBean polyline = new EncodedPolylineBean();
            polyline.setPoints(obaConsequence.getDiversionPath());
            details.setDiversionPath(polyline);
          }
          result.setConditionDetails(details);
        }
      }
      results.add(result);
    }

    situation.setConsequences(results);
  }

  private NaturalLanguageStringBean nls(DefaultedTextStructure text) {
    if (text == null)
      return null;
    String value = text.getValue();
    if (value == null || value.trim().isEmpty())
      return null;
    NaturalLanguageStringBean bean = new NaturalLanguageStringBean();
    bean.setValue(value);
    bean.setLang(text.getLang());
    return bean;
  }

  private class ClientTask implements Runnable {

    private boolean _connectionFailure = false;

    @Override
    public void run() {
      try {
        SiriClientRequest request = new SiriClientRequest();
        request.setTargetUrl(_url);
        Siri siri = new Siri();
        siri.setServiceRequest(new ServiceRequest());
        request.setPayload(siri);

        Siri delivery = _client.handleRequestWithResponse(request);

        processDelivery(delivery.getServiceDelivery());
        if (_connectionFailure)
          _log.info("siri connection re-establihed to" + _url);
        _connectionFailure = false;
      } catch (Exception ex) {
        if (!_connectionFailure)
          _log.warn("error processing siri request for url " + _url, ex);
        else
          _log.debug("error processing siri request for url " + _url, ex);
        _connectionFailure = true;
      }
    }
  }
}
