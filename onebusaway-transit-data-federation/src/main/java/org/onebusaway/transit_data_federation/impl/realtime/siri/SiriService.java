package org.onebusaway.transit_data_federation.impl.realtime.siri;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.Duration;

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.siri.AffectedApplicationStructure;
import org.onebusaway.siri.OneBusAwayAffects;
import org.onebusaway.siri.OneBusAwayAffectsStructure.Applications;
import org.onebusaway.siri.OneBusAwayConsequence;
import org.onebusaway.siri.core.ESiriModuleType;
import org.onebusaway.transit_data.model.service_alerts.ESensitivity;
import org.onebusaway.transit_data.model.service_alerts.ESeverity;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlertsService;
import org.onebusaway.transit_data_federation.services.service_alerts.Situation;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedAgency;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedApplication;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedCall;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedStop;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedVehicleJourney;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffects;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationConditionDetails;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationConsequence;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
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
import uk.org.siri.siri.BlockRefStructure;
import uk.org.siri.siri.DefaultedTextStructure;
import uk.org.siri.siri.EntryQualifierStructure;
import uk.org.siri.siri.ExtensionsStructure;
import uk.org.siri.siri.FramedVehicleJourneyRefStructure;
import uk.org.siri.siri.LocationStructure;
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
import uk.org.siri.siri.VehicleActivityStructure;
import uk.org.siri.siri.VehicleActivityStructure.MonitoredVehicleJourney;
import uk.org.siri.siri.VehicleJourneyRefStructure;
import uk.org.siri.siri.VehicleMonitoringDeliveryStructure;
import uk.org.siri.siri.VehicleRefStructure;
import uk.org.siri.siri.WorkflowStatusEnumeration;

@Component
public class SiriService {

  private TransitGraphDao _transitGraphDao;

  private ServiceAlertsService _serviceAlertsService;

  private VehicleLocationListener _vehicleLocationListener;

  private BlockCalendarService _blockCalendarService;

  /**
   * Time, in minutes,
   */
  private int _blockInstanceSearchWindow = 30;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setBlockCalendarService(BlockCalendarService blockCalendarService) {
    _blockCalendarService = blockCalendarService;
  }

  @Autowired
  public void setServiceAlertService(ServiceAlertsService serviceAlertsService) {
    _serviceAlertsService = serviceAlertsService;
  }

  @Autowired
  public void set(VehicleLocationListener vehicleLocationListener) {
    _vehicleLocationListener = vehicleLocationListener;
  }

  /**
   * @param blockInstanceSearchWindow time, in minutes
   */
  public void setBlockInstanceSearchWindow(int blockInstanceSearchWindow) {
    _blockInstanceSearchWindow = blockInstanceSearchWindow;
  }

  public synchronized void handleServiceDelivery(
      ServiceDelivery serviceDelivery,
      AbstractServiceDeliveryStructure deliveryForModule,
      ESiriModuleType moduleType, SiriEndpointDetails endpointDetails) {

    switch (moduleType) {
      case VEHICLE_MONITORING:
        handleVehicleMonitoring(serviceDelivery,
            (VehicleMonitoringDeliveryStructure) deliveryForModule,
            endpointDetails);
        break;
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

  private void handleVehicleMonitoring(ServiceDelivery serviceDelivery,
      VehicleMonitoringDeliveryStructure deliveryForModule,
      SiriEndpointDetails endpointDetails) {

    List<VehicleLocationRecord> records = new ArrayList<VehicleLocationRecord>();

    Date now = new Date();
    long timeFrom = now.getTime() - _blockInstanceSearchWindow * 60 * 1000;
    long timeTo = now.getTime() + _blockInstanceSearchWindow * 60 * 1000;

    for (VehicleActivityStructure vehicleActivity : deliveryForModule.getVehicleActivity()) {

      Date time = vehicleActivity.getRecordedAtTime();
      if (time == null)
        time = now;
      

      MonitoredVehicleJourney mvj = vehicleActivity.getMonitoredVehicleJourney();

      Duration delay = mvj.getDelay();
      if (delay == null)
        continue;

      VehicleRefStructure vehicleRef = mvj.getVehicleRef();
      if (vehicleRef == null || vehicleRef.getValue() == null)
        continue;

      BlockEntry block = getBlockForMonitoredVehicleJourney(mvj,
          endpointDetails);
      if (block == null) {
        TripEntry trip = getTripForMonitoredVehicleJourney(mvj, endpointDetails);
        if (trip != null)
          block = trip.getBlock();
      }

      if (block == null)
        continue;

      List<BlockInstance> instances = _blockCalendarService.getActiveBlocks(
          block.getId(), timeFrom, timeTo);

      // TODO : We currently assume that a block won't overlap with itself
      if (instances.size() != 1)
        continue;

      BlockInstance instance = instances.get(0);
      
      VehicleLocationRecord r = new VehicleLocationRecord();
      r.setTimeOfRecord(time.getTime());
      r.setServiceDate(instance.getServiceDate());
      r.setBlockId(block.getId());

      String agencyId = block.getId().getAgencyId();
      r.setVehicleId(new AgencyAndId(agencyId, vehicleRef.getValue()));

      r.setScheduleDeviation(delay.getTimeInMillis(now) / 1000);

      LocationStructure location = mvj.getVehicleLocation();
      if (location != null) {
        r.setCurrentLocationLat(location.getLatitude().doubleValue());
        r.setCurrentLocationLon(location.getLongitude().doubleValue());
      }

      records.add(r);
    }

    if (!records.isEmpty())
      _vehicleLocationListener.handleVehicleLocationRecords(records);
  }

  private BlockEntry getBlockForMonitoredVehicleJourney(
      MonitoredVehicleJourney mvj, SiriEndpointDetails endpointDetails) {

    BlockRefStructure blockRef = mvj.getBlockRef();
    if (blockRef == null || blockRef.getValue() == null)
      return null;

    for (String agencyId : endpointDetails.getDefaultAgencyIds()) {
      AgencyAndId blockId = new AgencyAndId(agencyId, blockRef.getValue());
      BlockEntry blockEntry = _transitGraphDao.getBlockEntryForId(blockId);
      if (blockEntry != null)
        return blockEntry;
    }

    /**
     * Try parsing the id itself
     */
    try {
      AgencyAndId blockId = AgencyAndId.convertFromString(blockRef.getValue());
      return _transitGraphDao.getBlockEntryForId(blockId);
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }

  private TripEntry getTripForMonitoredVehicleJourney(
      MonitoredVehicleJourney mvj, SiriEndpointDetails endpointDetails) {

    FramedVehicleJourneyRefStructure fvjRef = mvj.getFramedVehicleJourneyRef();
    if (fvjRef == null || fvjRef.getDatedVehicleJourneyRef() == null)
      return null;

    for (String agencyId : endpointDetails.getDefaultAgencyIds()) {
      AgencyAndId tripId = new AgencyAndId(agencyId,
          fvjRef.getDatedVehicleJourneyRef());
      TripEntry tripEntry = _transitGraphDao.getTripEntryForId(tripId);
      if (tripEntry != null)
        return tripEntry;
    }

    /**
     * Try parsing the id itself
     */
    try {
      AgencyAndId tripId = AgencyAndId.convertFromString(fvjRef.getDatedVehicleJourneyRef());
      return _transitGraphDao.getTripEntryForId(tripId);
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }

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

    if (!endpointDetails.getDefaultAgencyIds().isEmpty()) {
      String agencyId = endpointDetails.getDefaultAgencyIds().get(0);
      situation.setId(new AgencyAndId(agencyId, situationId));
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

    if (affectsStructure == null)
      return;

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

        if (!CollectionsLibrary.isEmpty(vj.getVehicleJourneyRef())) {
          List<AgencyAndId> tripIds = new ArrayList<AgencyAndId>();
          for (VehicleJourneyRefStructure vjRef : vj.getVehicleJourneyRef()) {
            AgencyAndId tripId = AgencyAndIdLibrary.convertFromString(vjRef.getValue());
            tripIds.add(tripId);
          }
          avj.setTripIds(tripIds);
        }

        avjs.add(avj);
      }

      situationAffects.setVehicleJourneys(avjs);
    }

    ExtensionsStructure extension = affectsStructure.getExtensions();
    if (extension != null && extension.getAny() != null) {
      Object ext = extension.getAny();
      if (ext instanceof OneBusAwayAffects) {
        OneBusAwayAffects obaAffects = (OneBusAwayAffects) ext;

        Applications applications = obaAffects.getApplications();
        if (applications != null
            && !CollectionsLibrary.isEmpty(applications.getAffectedApplication())) {

          List<SituationAffectedApplication> affectedApps = new ArrayList<SituationAffectedApplication>();
          List<AffectedApplicationStructure> apps = applications.getAffectedApplication();

          for (AffectedApplicationStructure sApp : apps) {
            SituationAffectedApplication app = new SituationAffectedApplication();
            app.setApiKey(sApp.getApiKey());
            affectedApps.add(app);
          }

          situationAffects.setApplications(affectedApps);
        }
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
