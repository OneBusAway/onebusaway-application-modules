package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedAgencyBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedCallBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedStopBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedVehicleJourneyBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;
import org.onebusaway.transit_data.model.service_alerts.SituationConditionDetailsBean;
import org.onebusaway.transit_data.model.service_alerts.SituationConsequenceBean;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.beans.ServiceAlertsBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlertsService;
import org.onebusaway.transit_data_federation.services.service_alerts.Situation;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedAgency;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedCall;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedStop;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedVehicleJourney;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffects;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationConditionDetails;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationConsequence;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceAlertsBeanServiceImpl implements ServiceAlertsBeanService {

  private ServiceAlertsService _serviceAlertsService;

  @Autowired
  public void setServiceAlertsService(ServiceAlertsService serviceAlertsService) {
    _serviceAlertsService = serviceAlertsService;
  }

  @Override
  public SituationBean createServiceAlert(String agencyId,
      SituationBean situationBean) {
    Situation situation = getBeanAsSituation(situationBean);
    situation = _serviceAlertsService.createServiceAlert(agencyId, situation);
    return getSituationAsBean(situation);
  }

  @Override
  public void updateServiceAlert(SituationBean situationBean) {
    Situation situation = getBeanAsSituation(situationBean);
    _serviceAlertsService.updateServiceAlert(situation);
  }

  @Override
  public void removeServiceAlert(AgencyAndId situationId) {
    _serviceAlertsService.removeServiceAlert(situationId);
  }

  @Override
  public SituationBean getServiceAlertForId(AgencyAndId situationId) {
    Situation situation = _serviceAlertsService.getServiceAlertForId(situationId);
    if (situation == null)
      return null;
    return getSituationAsBean(situation);
  }

  @Override
  public List<SituationBean> getAllSituationsForAgencyId(String agencyId) {

    List<Situation> situations = _serviceAlertsService.getAllSituationsForAgencyId(agencyId);

    return list(situations);
  }

  @Override
  public List<SituationBean> getSituationsForStopId(long time,
      AgencyAndId stopId) {

    List<Situation> situations = _serviceAlertsService.getSituationsForStopId(
        time, stopId);

    return list(situations);
  }

  @Override
  public List<SituationBean> getSituationsForStopCall(long time,
      BlockInstance blockInstance, BlockStopTimeEntry blockStopTime,
      AgencyAndId vehicleId) {

    List<Situation> situations = _serviceAlertsService.getSituationsForStopCall(
        time, blockInstance, blockStopTime, vehicleId);

    return list(situations);
  }

  @Override
  public List<SituationBean> getSituationsForVehicleJourney(long time,
      BlockInstance blockInstance, BlockTripEntry blockTrip,
      AgencyAndId vehicleId) {

    List<Situation> situations = _serviceAlertsService.getSituationsForVehicleJourney(
        time, blockInstance, blockTrip, vehicleId);

    return list(situations);
  }

  /****
   * Private Methods
   ****/

  private List<SituationBean> list(List<Situation> situations) {
    List<SituationBean> beans = new ArrayList<SituationBean>();
    for (Situation situation : situations)
      beans.add(getSituationAsBean(situation));
    return beans;
  }

  private SituationBean getSituationAsBean(Situation situation) {

    SituationBean bean = new SituationBean();

    bean.setId(AgencyAndIdLibrary.convertToString(situation.getId()));
    bean.setCreationTime(situation.getCreationTime());

    /**
     * Reasons
     */
    bean.setEnvironmentReason(situation.getEnvironmentReason());
    bean.setEquipmentReason(situation.getEquipmentReason());
    bean.setMiscellaneousReason(situation.getMiscellaneousReason());
    bean.setPersonnelReason(situation.getPersonnelReason());

    /**
     * Text descriptions
     */
    bean.setSummary(situation.getSummary());
    bean.setDescription(situation.getDescription());
    bean.setAdvice(situation.getAdvice());
    bean.setDetail(situation.getDetail());
    bean.setInternal(situation.getInternal());

    if (situation.getAffects() != null)
      bean.setAffects(getSituationAffectsAsBean(situation.getAffects()));

    if (!CollectionsLibrary.isEmpty(situation.getConsequences())) {
      List<SituationConsequenceBean> consequences = new ArrayList<SituationConsequenceBean>();
      for (SituationConsequence consequence : situation.getConsequences()) {
        SituationConsequenceBean consequenceBean = getConsequenceAsBean(consequence);
        consequences.add(consequenceBean);
      }
      bean.setConsequences(consequences);
    }

    return bean;
  }

  private Situation getBeanAsSituation(SituationBean bean) {
    Situation situation = new Situation();

    situation.setId(AgencyAndIdLibrary.convertFromString(bean.getId()));
    situation.setCreationTime(bean.getCreationTime());

    /**
     * Reasons
     */
    situation.setEnvironmentReason(bean.getEnvironmentReason());
    situation.setEquipmentReason(bean.getEquipmentReason());
    situation.setMiscellaneousReason(bean.getMiscellaneousReason());
    situation.setPersonnelReason(bean.getPersonnelReason());

    /**
     * Text descriptions
     */
    situation.setSummary(bean.getSummary());
    situation.setDescription(bean.getDescription());
    situation.setAdvice(bean.getAdvice());
    situation.setDetail(bean.getDetail());
    situation.setInternal(bean.getInternal());

    if (bean.getAffects() != null)
      situation.setAffects(getBeanAsSituationAffects(bean.getAffects()));

    if (!CollectionsLibrary.isEmpty(bean.getConsequences())) {
      List<SituationConsequence> consequences = new ArrayList<SituationConsequence>();
      for (SituationConsequenceBean consequenceBean : bean.getConsequences()) {
        SituationConsequence consequence = getBeanAsConsequence(consequenceBean);
        consequences.add(consequence);
      }
      situation.setConsequences(consequences);
    }

    return situation;
  }

  /****
   * Situations Affects
   ****/

  private SituationAffectsBean getSituationAffectsAsBean(
      SituationAffects affects) {

    SituationAffectsBean bean = new SituationAffectsBean();

    List<SituationAffectedAgency> agencies = affects.getAgencies();
    if (!CollectionsLibrary.isEmpty(agencies)) {
      List<SituationAffectedAgencyBean> beans = new ArrayList<SituationAffectedAgencyBean>();
      for (SituationAffectedAgency agency : agencies) {
        SituationAffectedAgencyBean agencyBean = getAffectedAgencyAsBean(agency);
        beans.add(agencyBean);
      }
      bean.setAgencies(beans);
    }

    List<SituationAffectedStop> stops = affects.getStops();
    if (!CollectionsLibrary.isEmpty(stops)) {
      List<SituationAffectedStopBean> beans = new ArrayList<SituationAffectedStopBean>();
      for (SituationAffectedStop stop : stops) {
        SituationAffectedStopBean stopBean = getAffectedStopAsBean(stop);
        beans.add(stopBean);
      }
      bean.setStops(beans);
    }

    List<SituationAffectedVehicleJourney> vehicleJourneys = affects.getVehicleJourneys();
    if (!CollectionsLibrary.isEmpty(vehicleJourneys)) {
      List<SituationAffectedVehicleJourneyBean> beans = new ArrayList<SituationAffectedVehicleJourneyBean>();
      for (SituationAffectedVehicleJourney vehicleJourney : vehicleJourneys) {
        SituationAffectedVehicleJourneyBean vjBean = getAffectedVehicleJourneyAsBean(vehicleJourney);
        beans.add(vjBean);
      }
      bean.setVehicleJourneys(beans);
    }
    return bean;
  }

  private SituationAffects getBeanAsSituationAffects(SituationAffectsBean bean) {

    SituationAffects affects = new SituationAffects();

    List<SituationAffectedAgencyBean> agencyBeans = bean.getAgencies();
    if (!CollectionsLibrary.isEmpty(agencyBeans)) {
      List<SituationAffectedAgency> agencies = new ArrayList<SituationAffectedAgency>();
      for (SituationAffectedAgencyBean agencyBean : agencyBeans) {
        SituationAffectedAgency agency = getBeanAsAffectedAgency(agencyBean);
        agencies.add(agency);
      }
      affects.setAgencies(agencies);
    }

    List<SituationAffectedStopBean> stopBeans = bean.getStops();
    if (!CollectionsLibrary.isEmpty(stopBeans)) {
      List<SituationAffectedStop> stops = new ArrayList<SituationAffectedStop>();
      for (SituationAffectedStopBean stopBean : stopBeans) {
        SituationAffectedStop stop = getBeanAsAffectedStop(stopBean);
        stops.add(stop);
      }
      affects.setStops(stops);
    }

    List<SituationAffectedVehicleJourneyBean> vjBeans = bean.getVehicleJourneys();
    if (!CollectionsLibrary.isEmpty(vjBeans)) {
      List<SituationAffectedVehicleJourney> vehicleJourneys = new ArrayList<SituationAffectedVehicleJourney>();
      for (SituationAffectedVehicleJourneyBean vjBean : vjBeans) {
        SituationAffectedVehicleJourney vj = getBeanAsSituationAffectedVehicleJourney(vjBean);
        vehicleJourneys.add(vj);
      }
      affects.setVehicleJourneys(vehicleJourneys);
    }
    return affects;
  }

  /****
   * Affected Agency
   ****/

  private SituationAffectedAgencyBean getAffectedAgencyAsBean(
      SituationAffectedAgency agency) {

    SituationAffectedAgencyBean bean = new SituationAffectedAgencyBean();
    bean.setAgencyId(agency.getAgencyId());
    return bean;
  }

  private SituationAffectedAgency getBeanAsAffectedAgency(
      SituationAffectedAgencyBean agencyBean) {

    SituationAffectedAgency agency = new SituationAffectedAgency();
    agency.setAgencyId(agencyBean.getAgencyId());
    return agency;
  }

  /****
   * Affected Stop
   ****/

  private SituationAffectedStopBean getAffectedStopAsBean(
      SituationAffectedStop stop) {

    SituationAffectedStopBean bean = new SituationAffectedStopBean();
    bean.setStopId(AgencyAndIdLibrary.convertToString(stop.getStopId()));
    return bean;
  }

  private SituationAffectedStop getBeanAsAffectedStop(
      SituationAffectedStopBean stopBean) {

    SituationAffectedStop stop = new SituationAffectedStop();
    stop.setStopId(AgencyAndIdLibrary.convertFromString(stopBean.getStopId()));
    return stop;
  }

  /****
   * Affected Vehicle Journey
   ****/

  private SituationAffectedVehicleJourneyBean getAffectedVehicleJourneyAsBean(
      SituationAffectedVehicleJourney vehicleJourney) {

    SituationAffectedVehicleJourneyBean bean = new SituationAffectedVehicleJourneyBean();
    bean.setLineId(AgencyAndIdLibrary.convertToString(vehicleJourney.getLineId()));
    bean.setDirection(vehicleJourney.getDirectionId());

    List<SituationAffectedCall> calls = vehicleJourney.getCalls();
    if (!CollectionsLibrary.isEmpty(calls)) {
      List<SituationAffectedCallBean> beans = new ArrayList<SituationAffectedCallBean>();
      for (SituationAffectedCall call : calls)
        beans.add(getAffectedCallAsBean(call));
      bean.setCalls(beans);
    }
    return bean;
  }

  private SituationAffectedVehicleJourney getBeanAsSituationAffectedVehicleJourney(
      SituationAffectedVehicleJourneyBean bean) {

    SituationAffectedVehicleJourney vehicleJourney = new SituationAffectedVehicleJourney();
    vehicleJourney.setLineId(AgencyAndIdLibrary.convertFromString(bean.getLineId()));
    vehicleJourney.setDirectionId(bean.getDirection());

    List<SituationAffectedCallBean> beans = bean.getCalls();
    if (!CollectionsLibrary.isEmpty(beans)) {
      List<SituationAffectedCall> calls = new ArrayList<SituationAffectedCall>();
      for (SituationAffectedCallBean callBean : beans)
        calls.add(getBeanAsAffectedCall(callBean));
      vehicleJourney.setCalls(calls);
    }
    return vehicleJourney;
  }

  /****
   * Affected Call
   ****/

  private SituationAffectedCallBean getAffectedCallAsBean(
      SituationAffectedCall affectedCall) {
    SituationAffectedCallBean bean = new SituationAffectedCallBean();
    bean.setStopId(AgencyAndIdLibrary.convertToString(affectedCall.getStopId()));
    return bean;
  }

  private SituationAffectedCall getBeanAsAffectedCall(
      SituationAffectedCallBean bean) {
    SituationAffectedCall call = new SituationAffectedCall();
    call.setStopId(AgencyAndIdLibrary.convertFromString(bean.getStopId()));
    return call;
  }

  /****
   * Consequence
   ****/

  private SituationConsequenceBean getConsequenceAsBean(
      SituationConsequence consequence) {

    SituationConsequenceBean bean = new SituationConsequenceBean();

    bean.setCondition(consequence.getCondition());

    SituationConditionDetails conditionDetails = consequence.getConditionDetails();
    if (conditionDetails != null)
      bean.setConditionDetails(getConditionDetailsAsBean(conditionDetails));

    return bean;
  }

  private SituationConsequence getBeanAsConsequence(
      SituationConsequenceBean consequenceBean) {

    SituationConsequence consequence = new SituationConsequence();

    consequence.setCondition(consequenceBean.getCondition());

    SituationConditionDetailsBean conditionDetails = consequenceBean.getConditionDetails();
    if (conditionDetails != null)
      consequence.setConditionDetails(getBeanAsConditionDetails(conditionDetails));

    return consequence;
  }

  /****
   * Condition Details
   ****/

  public SituationConditionDetailsBean getConditionDetailsAsBean(
      SituationConditionDetails details) {

    SituationConditionDetailsBean bean = new SituationConditionDetailsBean();

    bean.setDiversionPath(details.getDiversionPath());

    List<AgencyAndId> diversionStops = details.getDiversionStops();

    if (!CollectionsLibrary.isEmpty(diversionStops)) {

      List<String> stopIds = new ArrayList<String>();
      for (AgencyAndId stopId : diversionStops)
        stopIds.add(AgencyAndIdLibrary.convertToString(stopId));
      bean.setDiversionStopIds(stopIds);
    }

    return bean;
  }

  private SituationConditionDetails getBeanAsConditionDetails(
      SituationConditionDetailsBean bean) {

    SituationConditionDetails details = new SituationConditionDetails();

    details.setDiversionPath(bean.getDiversionPath());

    List<String> stopIds = bean.getDiversionStopIds();
    if (!CollectionsLibrary.isEmpty(stopIds)) {
      List<AgencyAndId> diversionStops = new ArrayList<AgencyAndId>();
      for (String stopId : stopIds)
        diversionStops.add(AgencyAndIdLibrary.convertFromString(stopId));
      details.setDiversionStops(diversionStops);
    }

    return details;
  }
}
