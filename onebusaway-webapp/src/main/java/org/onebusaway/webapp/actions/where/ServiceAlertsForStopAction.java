package org.onebusaway.webapp.actions.where;

import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.webapp.actions.OneBusAwayActionSupport;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

public class ServiceAlertsForStopAction extends OneBusAwayActionSupport {

  private static final long serialVersionUID = 1L;

  private TransitDataService _transitDataService;

  private List<String> _stopIds;

  private List<SituationBean> _situations;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @RequiredStringValidator(key = "requiredField")
  public void setStopId(List<String> stopIds) {
    _stopIds = stopIds;
  }

  public List<SituationBean> getSituations() {
    return _situations;
  }

  @Override
  @Actions({
      @Action(value = "/where/standard/service-alerts-for-stop"),
      @Action(value = "/where/iphone/service-alerts-for-stop"),
      @Action(value = "/where/text/service-alerts-for-stop")})
  public String execute() {
    SituationQueryBean query = new SituationQueryBean();
    query.setStopIds(_stopIds);
    ListBean<SituationBean> list = _transitDataService.getServiceAlerts(query);
    _situations = list.getList();
    return SUCCESS;
  }
}
