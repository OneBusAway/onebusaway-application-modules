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
package org.onebusaway.webapp.actions.admin.console;

import java.io.IOException;
import java.util.Map;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.json.JSONException;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.presentation.bundles.ResourceBundleSupport;
import org.onebusaway.presentation.bundles.service_alerts.Reasons;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedAgencyBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedApplicationBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedCallBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedStopBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedVehicleJourneyBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationConditionDetailsBean;
import org.onebusaway.transit_data.model.service_alerts.SituationConsequenceBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import com.thoughtworks.xstream.XStream;

@Results({
    @Result(type = "redirectAction", name = "submitSuccess", params = {
        "actionName", "service-alert", "id", "${id}", "parse", "true"}),
    @Result(type = "redirectAction", name = "deleteSuccess", params = {
        "actionName", "service-alerts!agency", "agencyId", "${agencyId}",
        "parse", "true"})})
public class ServiceAlertAction extends ActionSupport implements
    ModelDriven<ServiceAlertBean> {

  private static final long serialVersionUID = 1L;

  private TransitDataService _transitDataService;

  private ServiceAlertBean _model = new ServiceAlertBean();

  private String _agencyId;

  private String _raw;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Override
  public ServiceAlertBean getModel() {
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

    _model.setReason(string(_model.getReason()));

    if (_raw != null && !_raw.trim().isEmpty()) {
      ServiceAlertBean rawSituation = getStringAsRawSituation(_raw);
      _model.setAllAffects(rawSituation.getAllAffects());
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

  public Map<String, String> getReasonValues() {
    return ResourceBundleSupport.getLocaleMap(this, Reasons.class);
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

  private ServiceAlertBean getStringAsRawSituation(String value)
      throws IOException, JSONException {

    if (value == null || value.trim().isEmpty())
      return new ServiceAlertBean();

    XStream xstream = createXStream();
    return (ServiceAlertBean) xstream.fromXML(value);
  }

  private XStream createXStream() {

    XStream xstream = new XStream();

    xstream.alias("serviceAlert", ServiceAlertBean.class);
    xstream.alias("affects", SituationAffectsBean.class);
    xstream.alias("consequence", SituationConsequenceBean.class);

    return xstream;
  }
}
