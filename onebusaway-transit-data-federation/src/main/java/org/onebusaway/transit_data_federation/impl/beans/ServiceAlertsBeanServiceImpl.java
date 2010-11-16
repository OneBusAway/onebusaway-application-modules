package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedCallBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedStopBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedVehicleJourneyBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.beans.ServiceAlertsBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlertsService;
import org.onebusaway.transit_data_federation.services.service_alerts.Situation;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedCall;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedStop;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedVehicleJourney;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffects;
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

    bean.setAffects(getSituationAffectsAsBean(situation.getAffects()));

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

    situation.setAffects(getBeanAsSituationAffects(bean.getAffects()));

    return situation;
  }

  /****
   * Situations Affects
   ****/

  private SituationAffectsBean getSituationAffectsAsBean(
      SituationAffects affects) {

    SituationAffectsBean bean = new SituationAffectsBean();

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
    bean.setDirection(vehicleJourney.getDirection());

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
    vehicleJourney.setDirection(bean.getDirection());

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
}
