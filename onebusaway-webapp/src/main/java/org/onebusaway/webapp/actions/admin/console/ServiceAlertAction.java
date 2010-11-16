package org.onebusaway.webapp.actions.admin.console;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.json.JSONException;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedCallBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedStopBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedVehicleJourneyBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.webapp.actions.admin.console.reasons.EnvironmentReasons;
import org.onebusaway.webapp.actions.admin.console.reasons.EquipmentReasons;
import org.onebusaway.webapp.actions.admin.console.reasons.MiscellaneousReasons;
import org.onebusaway.webapp.actions.admin.console.reasons.PersonnelReasons;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.TextProvider;
import com.opensymphony.xwork2.TextProviderFactory;
import com.thoughtworks.xstream.XStream;

@Results({
    @Result(type = "redirectAction", name = "submitSuccess", params = {
        "actionName", "service-alert", "id", "${id}", "parse", "true"}),
    @Result(type = "redirectAction", name = "deleteSuccess", params = {
        "actionName", "service-alerts!agency", "agencyId", "${agencyId}",
        "parse", "true"})})
public class ServiceAlertAction extends ActionSupport implements
    ModelDriven<SituationBean> {

  private static final long serialVersionUID = 1L;

  private TransitDataService _transitDataService;

  private SituationBean _model = new SituationBean();

  private String _agencyId;

  private String _affectsRaw;

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

  public void setAffectsRaw(String affectsRaw) {
    _affectsRaw = affectsRaw;
  }

  public String getAffectsRaw() {
    return _affectsRaw;
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

    _affectsRaw = getSituationAffectsAsString();

    return SUCCESS;
  }

  public String submit() throws IOException, JSONException {

    _model.setEnvironmentReason(string(_model.getEnvironmentReason()));
    _model.setEquipmentReason(string(_model.getEquipmentReason()));
    _model.setPersonnelReason(string(_model.getPersonnelReason()));
    _model.setMiscellaneousReason(string(_model.getMiscellaneousReason()));
    _model.setUndefinedReason(string(_model.getUndefinedReason()));

    if (_affectsRaw != null && !_affectsRaw.trim().isEmpty())
      _model.setAffects(getStringAsSituationAffects(_affectsRaw));

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
    return getLocaleMap(EnvironmentReasons.class);
  }

  public Map<String, String> getEquipmentReasonValues() {
    return getLocaleMap(EquipmentReasons.class);
  }

  public Map<String, String> getMiscellaneousReasonValues() {
    return getLocaleMap(MiscellaneousReasons.class);
  }

  public Map<String, String> getPersonnelReasonValues() {
    return getLocaleMap(PersonnelReasons.class);
  }

  /****
   * 
   ****/

  private String string(String value) {
    if (value == null || value.isEmpty() || value.equals("null"))
      return null;
    return value;
  }

  private Map<String, String> getLocaleMap(Class<?> resourceType) {
    TextProviderFactory factory = new TextProviderFactory();
    TextProvider provider = factory.createInstance(resourceType, this);
    ResourceBundle bundle = provider.getTexts();
    Map<String, String> m = new TreeMap<String, String>();
    for (Enumeration<String> en = bundle.getKeys(); en.hasMoreElements();) {
      String key = en.nextElement();
      String value = bundle.getString(key);
      m.put(key, value);
    }
    return m;
  }

  private String getSituationAffectsAsString() {

    SituationAffectsBean affects = _model.getAffects();

    if (affects != null) {
      XStream xstream = createXStream();
      return xstream.toXML(affects);
    }

    return null;
  }

  private SituationAffectsBean getStringAsSituationAffects(String value)
      throws IOException, JSONException {

    if (value == null || value.trim().isEmpty())
      return new SituationAffectsBean();

    XStream xstream = createXStream();
    return (SituationAffectsBean) xstream.fromXML(value);
  }

  private XStream createXStream() {
    XStream xstream = new XStream();
    xstream.alias("affects", SituationAffectsBean.class);
    xstream.alias("stop", SituationAffectedStopBean.class);
    xstream.alias("vehicleJourney",
        SituationAffectedVehicleJourneyBean.class);
    xstream.alias("call", SituationAffectedCallBean.class);
    return xstream;
  }
}
