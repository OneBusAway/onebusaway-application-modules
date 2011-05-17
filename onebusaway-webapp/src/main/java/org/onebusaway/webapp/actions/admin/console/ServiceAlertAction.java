package org.onebusaway.webapp.actions.admin.console;

import java.io.IOException;
import java.util.Map;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.json.JSONException;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.presentation.bundles.ResourceBundleSupport;
import org.onebusaway.presentation.bundles.service_alerts.EnvironmentReasons;
import org.onebusaway.presentation.bundles.service_alerts.EquipmentReasons;
import org.onebusaway.presentation.bundles.service_alerts.MiscellaneousReasons;
import org.onebusaway.presentation.bundles.service_alerts.PersonnelReasons;
import org.onebusaway.presentation.bundles.service_alerts.Sensitivity;
import org.onebusaway.presentation.bundles.service_alerts.Severity;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedAgencyBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedApplicationBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedCallBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedStopBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedVehicleJourneyBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;
import org.onebusaway.transit_data.model.service_alerts.SituationConditionDetailsBean;
import org.onebusaway.transit_data.model.service_alerts.SituationConsequenceBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.webapp.actions.OneBusAwayActionSupport;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.thoughtworks.xstream.XStream;

@Results({
    @Result(type = "redirectAction", name = "submitSuccess", params = {
        "actionName", "service-alert", "id", "${id}", "parse", "true"}),
    @Result(type = "redirectAction", name = "deleteSuccess", params = {
        "actionName", "service-alerts!agency", "agencyId", "${agencyId}",
        "parse", "true"})})
public class ServiceAlertAction extends OneBusAwayActionSupport implements
    ModelDriven<SituationBean> {

  private static final long serialVersionUID = 1L;

  private TransitDataService _transitDataService;

  private SituationBean _model = new SituationBean();

  private String _agencyId;

  private String _raw;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Override
  public SituationBean getModel() {
    return _model;
  }

  public void setAgencyId(String agencyId) {
    _agencyId = agencyId;
  }

  public String getAgencyId() {
    return _agencyId;
  }

  public void setRaw(String raw) {
    _raw = raw;
  }

  public String getRaw() {
    return _raw;
  }

  @Override
  public String execute() {

    if (_model.getId() != null)
      _model = _transitDataService.getServiceAlertForId(_model.getId());

    if (_agencyId == null && _model.getId() != null) {
      String id = _model.getId();
      int index = id.indexOf('_');
      if (index != -1)
        _agencyId = id.substring(0, index);
    }

    _raw = getRawSituationAsString();

    return SUCCESS;
  }

  public String submit() throws IOException, JSONException {

    _model.setEnvironmentReason(string(_model.getEnvironmentReason()));
    _model.setEquipmentReason(string(_model.getEquipmentReason()));
    _model.setPersonnelReason(string(_model.getPersonnelReason()));
    _model.setMiscellaneousReason(string(_model.getMiscellaneousReason()));
    _model.setUndefinedReason(string(_model.getUndefinedReason()));

    if (_raw != null && !_raw.trim().isEmpty()) {
      SituationBean rawSituation = getStringAsRawSituation(_raw);
      _model.setAffects(rawSituation.getAffects());
      _model.setConsequences(rawSituation.getConsequences());
    }

    if (_model.getId() == null || _model.getId().trim().isEmpty())
      _model = _transitDataService.createServiceAlert(_agencyId, _model);
    else
      _transitDataService.updateServiceAlert(_model);

    return "submitSuccess";
  }

  public String delete() {

    if (_model.getId() != null) {
      _transitDataService.removeServiceAlert(_model.getId());
    }

    return "deleteSuccess";
  }

  /****
   * 
   ****/

  public Map<String, String> getEnvironmentReasonValues() {
    return ResourceBundleSupport.getLocaleMap(this, EnvironmentReasons.class);
  }

  public Map<String, String> getEquipmentReasonValues() {
    return ResourceBundleSupport.getLocaleMap(this, EquipmentReasons.class);
  }

  public Map<String, String> getMiscellaneousReasonValues() {
    return ResourceBundleSupport.getLocaleMap(this, MiscellaneousReasons.class);
  }

  public Map<String, String> getPersonnelReasonValues() {
    return ResourceBundleSupport.getLocaleMap(this, PersonnelReasons.class);
  }

  public Map<String, String> getSeverityValues() {
    return ResourceBundleSupport.getLocaleMap(this, Severity.class);
  }

  public Map<String, String> getSensitivityValues() {
    return ResourceBundleSupport.getLocaleMap(this, Sensitivity.class);
  }

  /****
   * 
   ****/

  private String string(String value) {
    if (value == null || value.isEmpty() || value.equals("null"))
      return null;
    return value;
  }

  private String getRawSituationAsString() {

    XStream xstream = createXStream();
    return xstream.toXML(_model);
  }

  private SituationBean getStringAsRawSituation(String value)
      throws IOException, JSONException {

    if (value == null || value.trim().isEmpty())
      return new SituationBean();

    XStream xstream = createXStream();
    return (SituationBean) xstream.fromXML(value);
  }

  private XStream createXStream() {

    XStream xstream = new XStream();

    xstream.alias("situation", SituationBean.class);
    xstream.alias("affects", SituationAffectsBean.class);
    xstream.alias("agency", SituationAffectedAgencyBean.class);
    xstream.alias("stop", SituationAffectedStopBean.class);
    xstream.alias("vehicleJourney", SituationAffectedVehicleJourneyBean.class);
    xstream.alias("call", SituationAffectedCallBean.class);
    xstream.alias("application", SituationAffectedApplicationBean.class);
    xstream.alias("consequence", SituationConsequenceBean.class);
    xstream.alias("conditionDetails", SituationConditionDetailsBean.class);
    xstream.alias("encodedPolyline", EncodedPolylineBean.class);

    return xstream;
  }
}
