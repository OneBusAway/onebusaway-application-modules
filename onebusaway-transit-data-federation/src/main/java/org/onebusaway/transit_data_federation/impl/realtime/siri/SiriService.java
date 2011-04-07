package org.onebusaway.transit_data_federation.impl.realtime.siri;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.siri.OneBusAwayConsequence;
import org.onebusaway.siri.core.ESiriModuleType;
import org.onebusaway.transit_data.model.service_alerts.ESensitivity;
import org.onebusaway.transit_data.model.service_alerts.ESeverity;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlertsService;
import org.onebusaway.transit_data_federation.services.service_alerts.Situation;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedAgency;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedCall;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedStop;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedVehicleJourney;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffects;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationConditionDetails;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationConsequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.org.siri.siri.AbstractServiceDeliveryStructure;
import uk.org.siri.siri.AffectedCallStructure;
import uk.org.siri.siri.AffectedOperatorStructure;
import uk.org.siri.siri.AffectedStopPointStructure;
import uk.org.siri.siri.AffectedVehicleJourneyStructure;
import uk.org.siri.siri.AffectedVehicleJourneyStructure.Calls;
import uk.org.siri.siri.AffectsScopeStructure;
import uk.org.siri.siri.AffectsScopeStructure.Operators;
import uk.org.siri.siri.AffectsScopeStructure.StopPoints;
import uk.org.siri.siri.AffectsScopeStructure.VehicleJourneys;
import uk.org.siri.siri.DefaultedTextStructure;
import uk.org.siri.siri.EntryQualifierStructure;
import uk.org.siri.siri.ExtensionsStructure;
import uk.org.siri.siri.OperatorRefStructure;
import uk.org.siri.siri.PtConsequenceStructure;
import uk.org.siri.siri.PtConsequencesStructure;
import uk.org.siri.siri.PtSituationElementStructure;
import uk.org.siri.siri.SensitivityEnumeration;
import uk.org.siri.siri.ServiceDelivery;
import uk.org.siri.siri.SeverityEnumeration;
import uk.org.siri.siri.SituationExchangeDeliveryStructure;
import uk.org.siri.siri.SituationExchangeDeliveryStructure.Situations;
import uk.org.siri.siri.StopPointRefStructure;
import uk.org.siri.siri.WorkflowStatusEnumeration;

@Component
public class SiriService {

  private ServiceAlertsService _serviceAlertsService;

  @Autowired
  public void setServiceAlertService(ServiceAlertsService serviceAlertsService) {
    _serviceAlertsService = serviceAlertsService;
  }

  public synchronized void handleServiceDelivery(
      ServiceDelivery serviceDelivery,
      AbstractServiceDeliveryStructure deliveryForModule,
      ESiriModuleType moduleType, SiriEndpointDetails endpointDetails) {

    switch (moduleType) {
      case SITUATION_EXCHANGE:
        handleSituationExchange(serviceDelivery,
            (SituationExchangeDeliveryStructure) deliveryForModule,
            endpointDetails);
        break;
    }

  }

  /****
   * Private Methods
   ****/

  private void handleSituationExchange(ServiceDelivery serviceDelivery,
      SituationExchangeDeliveryStructure sxDelivery,
      SiriEndpointDetails endpointDetails) {

    Situations situations = sxDelivery.getSituations();

    if (situations == null)
      return;

    List<Situation> situationsToUpdate = new ArrayList<Situation>();
    List<AgencyAndId> situationIdsToRemove = new ArrayList<AgencyAndId>();

    for (PtSituationElementStructure ptSituation : situations.getPtSituationElement()) {

      Situation situation = getPtSituationAsSituation(ptSituation,
          endpointDetails);

      AgencyAndId situationId = situation.getId();

      WorkflowStatusEnumeration progress = ptSituation.getProgress();
      boolean remove = (progress != null && (progress == WorkflowStatusEnumeration.CLOSING || progress == WorkflowStatusEnumeration.CLOSED));

      if (remove) {
        situationIdsToRemove.add(situationId);
      } else {
        situationsToUpdate.add(situation);
      }
    }

    _serviceAlertsService.updateServiceAlerts(situationsToUpdate);
    _serviceAlertsService.removeServiceAlerts(situationIdsToRemove);
  }

  private Situation getPtSituationAsSituation(
      PtSituationElementStructure ptSituation,
      SiriEndpointDetails endpointDetails) {

    Situation situation = new Situation();
    EntryQualifierStructure situationNumber = ptSituation.getSituationNumber();
    String situationId = situationNumber.getValue();

    if (endpointDetails.getAgencyId() != null) {
      situation.setId(new AgencyAndId(endpointDetails.getAgencyId(),
          situationId));
    } else {
      situation.setId(AgencyAndIdLibrary.convertFromString(situationId));
    }

    handleDescriptions(ptSituation, situation);
    handleOtherFields(ptSituation, situation);
    handlReasons(ptSituation, situation);
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

  /****
   * Affects
   ****/

  private void handleAffects(PtSituationElementStructure ptSituation,
      Situation situation) {

    AffectsScopeStructure affectsStructure = ptSituation.getAffects();

    if (affectsStructure != null) {

      SituationAffects situationAffects = new SituationAffects();
      situation.setAffects(situationAffects);

      Operators operators = affectsStructure.getOperators();

      if (operators != null
          && !CollectionsLibrary.isEmpty(operators.getAffectedOperator())) {

        List<SituationAffectedAgency> affectedAgencies = new ArrayList<SituationAffectedAgency>();

        for (AffectedOperatorStructure operator : operators.getAffectedOperator()) {
          OperatorRefStructure operatorRef = operator.getOperatorRef();
          if (operatorRef == null || operatorRef.getValue() == null)
            continue;
          String agencyId = operatorRef.getValue();
          SituationAffectedAgency affectedAgency = new SituationAffectedAgency();
          affectedAgency.setAgencyId(agencyId);
          affectedAgencies.add(affectedAgency);
        }

        if (!affectedAgencies.isEmpty())
          situationAffects.setAgencies(affectedAgencies);
      }

      StopPoints stopPoints = affectsStructure.getStopPoints();

      if (stopPoints != null
          && !CollectionsLibrary.isEmpty(stopPoints.getAffectedStopPoint())) {

        List<SituationAffectedStop> affectedStops = new ArrayList<SituationAffectedStop>();

        for (AffectedStopPointStructure stopPoint : stopPoints.getAffectedStopPoint()) {
          StopPointRefStructure stopRef = stopPoint.getStopPointRef();
          if (stopRef == null || stopRef.getValue() == null)
            continue;
          AgencyAndId stopId = AgencyAndIdLibrary.convertFromString(stopRef.getValue());
          SituationAffectedStop affectedStop = new SituationAffectedStop();
          affectedStop.setStopId(stopId);
          affectedStops.add(affectedStop);
        }

        if (!affectedStops.isEmpty())
          situationAffects.setStops(affectedStops);
      }

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

}
